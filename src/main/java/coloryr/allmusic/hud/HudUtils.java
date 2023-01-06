package coloryr.allmusic.hud;

import coloryr.allmusic.AllMusic;
import coloryr.allmusic.enums.HudPos;
import coloryr.allmusic.objs.hud.PosOBJ;
import coloryr.allmusic.objs.hud.SaveOBJ;
import coloryr.allmusic.objs.music.SongInfoObj;
import coloryr.allmusic.objs.music.LyricItemObj;
import coloryr.allmusic.music.play.PlayMusic;
import coloryr.allmusic.utils.Function;
import com.google.gson.Gson;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HudUtils {
    private static final Map<String, SaveOBJ> huds = new ConcurrentHashMap<>();

    public static SaveOBJ get(String name) {
        if (!huds.containsKey(name)) {
            SaveOBJ obj = AllMusic.getConfig().DefaultHud.copy();
            huds.put(name, obj);
            DataSql.task(() -> DataSql.addUser(name, obj));
            return obj;
        }
        return huds.get(name);
    }

    public static void add(String name, SaveOBJ hud) {
        huds.put(name, hud);
    }

    public static void addAndSave(String name, SaveOBJ hud) {
        huds.put(name, hud);
        DataSql.task(() -> DataSql.addUser(name, hud));
    }

    public static void save() {
        for (Map.Entry<String, SaveOBJ> item : huds.entrySet()) {
            DataSql.addUser(item.getKey(), item.getValue());
        }
    }

    /**
     * 设置玩家Hud位置
     * @param player 用户名
     * @param pos    位置
     * @param x      x
     * @param y      y
     * @return 位置数据
     */
    public static PosOBJ setHudPos(String player, String pos, String x, String y) {
        SaveOBJ obj = get(player);
        if (obj == null)
            obj = AllMusic.getConfig().DefaultHud.copy();
        HudPos pos1 = HudPos.valueOf(pos);
        PosOBJ posOBJ = new PosOBJ(0, 0);
        if (!Function.isInteger(x) && !Function.isInteger(y))
            return null;
        int x1 = Integer.parseInt(x);
        int y1 = Integer.parseInt(y);

        switch (pos1) {
            case lyric:
                posOBJ = obj.Lyric;
                break;
            case list:
                posOBJ = obj.List;
                break;
            case info:
                posOBJ = obj.Info;
                break;
            case pic:
                posOBJ = obj.Pic;
        }
        posOBJ.x = x1;
        posOBJ.y = y1;

        addAndSave(player, obj);
        AllMusic.save();
        HudUtils.sendHudPos(player);
        return posOBJ;
    }

    /**
     * 更新Hud的List数据
     */
    public static void sendHudListData() {
        String info;
        if (PlayMusic.getSize() == 0) {
            info = AllMusic.getMessage().Hud.NoList;
        } else {
            String now;
            StringBuilder list = new StringBuilder();
            for (SongInfoObj info1 : PlayMusic.getList()) {
                if (info1 == null)
                    continue;
                now = info1.getInfo();
                if (now.length() > AllMusic.getConfig().MessageLimitSize)
                    now = now.substring(0, AllMusic.getConfig().MessageLimitSize - 1) + "...";
                list.append(now).append("\n");
            }
            info = AllMusic.getMessage().Hud.List
                    .replace("%Size%", String.valueOf(PlayMusic.getList().size()))
                    .replace("%List%", list.toString());
        }

        AllMusic.side.sendHudList(info);
    }

    /**
     * 时间转换
     *
     * @param time 时间
     * @return 结果
     */
    private static String tranTime(int time) {
        int m = time / 60;
        int s = time - m * 60;
        return (m < 10 ? "0" : "") + m + ":" + (s < 10 ? "0" : "") + s;
    }

    /**
     * 更新Hud的信息数据
     */
    public static void sendHudNowData() {
        String info;
        if (PlayMusic.nowPlayMusic == null) {
            info = AllMusic.getMessage().Hud.NoMusic;
        } else {
            info = AllMusic.getMessage().Hud.Music
                    .replace("%Name%", PlayMusic.nowPlayMusic.getName())
                    .replace("%AllTime%", tranTime(PlayMusic.musicAllTime))
                    .replace("%NowTime%", tranTime(PlayMusic.musicNowTime / 1000))
                    .replace("%Author%", PlayMusic.nowPlayMusic.getAuthor())
                    .replace("%Alia%", PlayMusic.nowPlayMusic.getAlia())
                    .replace("%Al%", PlayMusic.nowPlayMusic.getAl())
                    .replace("%Player%", PlayMusic.nowPlayMusic.getCall());
        }

        AllMusic.side.sendHudInfo(info);
    }

    /**
     * 更新Hud的歌词数据
     *
     * @param showobj 显示的歌词
     */
    public static void sendHudLyricData(LyricItemObj showobj) {
        String info;
        if (showobj == null) {
            info = AllMusic.getMessage().Hud.NoLyric;
        } else {
            info = AllMusic.getMessage().Hud.Lyric
                    .replace("%Lyric%", showobj.getLyric() == null ? "" : showobj.getLyric())
                    .replace("%Tlyric%", (showobj.isHaveT() && showobj.getTlyric() != null) ?
                            showobj.getTlyric() : "");
        }

        AllMusic.side.sendHudLyric(info);
    }

    /**
     * 更新Hud的开启关闭数据
     *
     * @param player 用户名
     * @param pos    输入数据
     * @return 设置结果
     */
    public static boolean setHudEnable(String player, String pos) {
        SaveOBJ obj = get(player);
        boolean res = false;
        if (obj == null) {
            obj = AllMusic.getConfig().DefaultHud.copy();
            res = obj.EnableInfo && obj.EnableList && obj.EnableLyric;
        } else {
            if (pos == null) {
                if (obj.EnableInfo && obj.EnableList && obj.EnableLyric) {
                    obj.EnableInfo = false;
                    obj.EnableList = false;
                    obj.EnableLyric = false;
                    obj.EnablePic = false;
                } else {
                    obj.EnableInfo = true;
                    obj.EnableList = true;
                    obj.EnableLyric = true;
                    obj.EnablePic = true;
                    res = true;
                }
            } else {
                HudPos pos1 = HudPos.valueOf(pos);
                switch (pos1) {
                    case info:
                        obj.EnableInfo = !obj.EnableInfo;
                        break;
                    case list:
                        obj.EnableList = !obj.EnableList;
                        break;
                    case lyric:
                        obj.EnableLyric = !obj.EnableLyric;
                        break;
                    case pic:
                        obj.EnablePic = !obj.EnablePic;
                        break;
                }
            }
        }
        clearHud(player);
        addAndSave(player, obj);
        AllMusic.save();
        HudUtils.sendHudPos(player);
        if (pos == null) {
            return res;
        } else {
            HudPos pos1 = HudPos.valueOf(pos);
            switch (pos1) {
                case info:
                    return obj.EnableInfo;
                case list:
                    return obj.EnableList;
                case lyric:
                    return obj.EnableLyric;
                case pic:
                    return obj.EnablePic;
            }
        }
        return false;
    }

    /**
     * 清空Hud数据
     */
    public static void clearHud() {
        AllMusic.side.clearHud();
    }

    /**
     * 清空Hud数
     *
     * @param player 用户名
     */
    public static void clearHud(String player) {
        AllMusic.side.clearHud(player);
    }

    /**
     * 发送Hud位置信息
     *
     * @param player 用户名
     */
    public static void sendHudPos(String player) {
        AllMusic.side.runTask(() -> {
            try {
                SaveOBJ obj = get(player);
                if (obj == null) {
                    obj = AllMusic.getConfig().DefaultHud.copy();
                    addAndSave(player, obj);
                    AllMusic.save();
                }
                String data = new Gson().toJson(obj);
                AllMusic.side.send(data, player, null);
            } catch (Exception e1) {
                AllMusic.log.warning("§d[AllMusic]§c数据发送发生错误");
                e1.printStackTrace();
            }
        });
    }

    /**
     * 重置Hud位置
     *
     * @param player 用户名
     */
    public static void reset(String player) {
        SaveOBJ obj = AllMusic.getConfig().DefaultHud.copy();
        clearHud(player);
        addAndSave(player, obj);
        AllMusic.save();
        HudUtils.sendHudPos(player);
    }

    /**
     * 设置Hud的图片大小
     *
     * @param player 用户名
     * @param size   大小
     * @return 结果
     */
    public static boolean setPicSize(String player, String size) {
        SaveOBJ obj = get(player);
        if (obj == null)
            obj = AllMusic.getConfig().DefaultHud.copy();
        if (!Function.isInteger(size))
            return false;

        obj.PicSize = Integer.parseInt(size);

        addAndSave(player, obj);
        AllMusic.save();
        HudUtils.sendHudPos(player);
        return true;
    }
}
