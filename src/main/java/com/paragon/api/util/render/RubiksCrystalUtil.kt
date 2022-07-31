package com.paragon.api.util.render;

import org.lwjgl.util.vector.Quaternion;

/**
 * @author rebane2001
 */
public class RubiksCrystalUtil {

    public static Quaternion[] cubeletStatus = {
            new Quaternion(),
            new Quaternion(),
            new Quaternion(),
            new Quaternion(),
            new Quaternion(),
            new Quaternion(),
            new Quaternion(),
            new Quaternion(),
            new Quaternion(),
            new Quaternion(),
            new Quaternion(),
            new Quaternion(),
            new Quaternion(),
            new Quaternion(),
            new Quaternion(),
            new Quaternion(),
            new Quaternion(),
            new Quaternion(),
            new Quaternion(),
            new Quaternion(),
            new Quaternion(),
            new Quaternion(),
            new Quaternion(),
            new Quaternion(),
            new Quaternion(),
            new Quaternion(),
            new Quaternion(),
            new Quaternion()
    };

    public static int[][][] cubeletLookup = {
            {
                    { 17, 9, 0 },
                    { 20, 16, 3 },
                    { 23, 15, 6 }
            },

            {
                    { 18, 10, 1 },
                    { 21, -1, 4 },
                    { 24, 14, 7 }
            },

            {
                    { 19, 11, 2 },
                    { 22, 12, 5 },
                    { 25, 13, 8 }
            }
    };

    public static int[][] cubeSides = {
            { 0, 1, 2, 3, 4, 5, 6, 7, 8 },
            { 19, 18, 17, 22, 21, 20, 25, 24, 23 },
            { 0, 1, 2, 9, 10, 11, 17, 18, 19 },
            { 23, 24, 25, 15, 14, 13, 6, 7, 8 },
            { 17, 9, 0, 20, 16, 3, 23, 15, 6 },
            { 2, 11, 19, 5, 12, 22, 8, 13, 25 }
    };

    public static int[][] cubeSideTransforms = {
            { 0, 0, 1 },
            { 0, 0, -1 },
            { 0, 1, 0 },
            { 0, -1, 0 },
            { -1, 0, 0 },
            { 1, 0, 0 }
    };

}
