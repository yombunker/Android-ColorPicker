package com.bunk3r.colorpicker.perf;

public class TrackerId {

    public static final int COLOR_AREA_FPS = 1;
    public static final int HUE_AREA_FPS = 2;
    public static final int GENERATE_COLORS_ELAPSE = 3;
    public static final int GENERATE_MAIN_COLORS_ELAPSE = 4;
    public static final int GENERATE_HUE_ELAPSE = 5;

    private static final String COLOR_AREA_FPS_NAME = "COLOR AREA FPS";
    private static final String HUE_AREA_FPS_NAME = "HUE AREA FPS";
    private static final String GENERATE_COLORS_ELAPSE_NAME = "COLOR REFRESHING ELAPSE";
    private static final String GENERATE_MAIN_COLORS_ELAPSE_NAME = "MAIN COLOR ELAPSE";
    private static final String GENERATE_HUE_ELAPSE_NAME = "HUE GENERATION ELAPSE";
    private static final String UNKNOWN_NAME = "NO IDEA";

    public static String getNanme(int trackerId) {
        String name;

        switch (trackerId) {
            case COLOR_AREA_FPS:
                name = COLOR_AREA_FPS_NAME;
                break;
            case HUE_AREA_FPS:
                name = HUE_AREA_FPS_NAME;
                break;
            case GENERATE_COLORS_ELAPSE:
                name = GENERATE_COLORS_ELAPSE_NAME;
                break;
            case GENERATE_MAIN_COLORS_ELAPSE:
                name = GENERATE_MAIN_COLORS_ELAPSE_NAME;
                break;
            case GENERATE_HUE_ELAPSE:
                name = GENERATE_HUE_ELAPSE_NAME;
                break;
            default:
                name = UNKNOWN_NAME;
        }

        return name;
    }

}