package ru.ssau.image_processing.utils;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Bayerizator {

    public static void superpixel(
            final BufferedImage source,
            final BufferedImage result
    ) {
        for (int x = 0; x < source.getWidth() - 1; x+=2) {
            for (int y = 0; y < source.getHeight() - 1; y+=2) {

                /*
                0 1
                2 3
                */

                // Получаем цвет текущего пикселя
                Color color_0 = new Color(source.getRGB(x, y));
                Color color_1 = new Color(source.getRGB(x + 1, y));
                Color color_2 = new Color(source.getRGB(x, y + 1));
                Color color_3 = new Color(source.getRGB(x + 1, y + 1));

                // Получаем каналы этого цвета
                int blue = color_3.getBlue();
                int red = color_0.getRed();
                int green = (color_1.getGreen() + color_2.getGreen()) / 2;

                //  Cоздаем новый цвет
                Color newColor = new Color(red, green, blue);

                // И устанавливаем этот цвет в текущий пиксель результирующего изображения
                result.setRGB(x / 2, y / 2, newColor.getRGB());
            }
        }
    }

    public static void bilingual_interpolation(
            final BufferedImage source,
            final BufferedImage result
    ) {
        Color newColor;
        for (int x = 1; x < source.getWidth() - 1; x++) {
            for (int y = 1; y < source.getHeight() - 1; y++) {
                if (x % 2 == 0 && y % 2 == 0) { //Красный пиксель
                    final int red = new Color(source.getRGB(x, y)).getRed();
                    final int green = horizontalInterpolation(x, y, source, ColorChoose.GREEN);
                    final int blue = diagInterpolation(x, y, source, ColorChoose.BLUE);
                    newColor = new Color(red, green, blue);
                    result.setRGB(x, y, newColor.getRGB());
                } else if (x % 2 == 1 && y % 2 == 1) { //Синий пиксель
                    final int blue = new Color(source.getRGB(x, y)).getBlue();
                    final int green = horizontalInterpolation(x, y, source, ColorChoose.GREEN);
                    final int red = diagInterpolation(x, y, source, ColorChoose.RED);
                    newColor = new Color(red, green, blue);
                    result.setRGB(x, y, newColor.getRGB());
                } else if (x % 2 == 1) { //Зелёные 2 типа
                    //Сверху и снизу - синие, слева и справа - красные
                    final int green = new Color(source.getRGB(x, y)).getGreen();
                    final int blue = (
                            new Color(source.getRGB(x, y + 1)).getBlue()
                                    + new Color(source.getRGB(x, y - 1)).getBlue()
                    ) / 2;
                    final int red = (
                            new Color(source.getRGB(x - 1, y )).getRed()
                                    + new Color(source.getRGB(x + 1, y)).getRed()
                    ) / 2;
                    newColor = new Color(red, green, blue);
                    result.setRGB(x, y, newColor.getRGB());
                } else {
                    //Сверху и снизу - красные, слева и справа - синие
                    final int green = new Color(source.getRGB(x, y)).getGreen();
                    final int red = (
                            new Color(source.getRGB(x, y + 1)).getRed()
                                    + new Color(source.getRGB(x, y - 1)).getRed()
                    ) / 2;
                    final int blue = (
                            new Color(source.getRGB(x - 1, y )).getBlue()
                                    + new Color(source.getRGB(x + 1, y)).getBlue()
                    ) / 2;
                    newColor = new Color(red, green, blue);
                    result.setRGB(x, y, newColor.getRGB());
                }
            }
        }
        copyEndPixels(result); //Копирование краевых пикселей
    }

    public static int horizontalInterpolation(final int x, final int y, final BufferedImage source, final ColorChoose ch) {
        int sum = 0;
        Color color = new Color(source.getRGB(x - 1, y));
        sum += getColorComponent(ch, color);
        color = new Color(source.getRGB(x + 1, y));
        sum += getColorComponent(ch, color);
        color = new Color(source.getRGB(x, y - 1));
        sum += getColorComponent(ch, color);
        color = new Color(source.getRGB(x, y + 1));
        sum += getColorComponent(ch, color);
        return sum / 4;
    }

    public static int diagInterpolation(final int x, final int y, final BufferedImage source, final ColorChoose ch) {
        int sum = 0;
        Color color = new Color(source.getRGB(x - 1, y - 1));
        sum += getColorComponent(ch, color);
        color = new Color(source.getRGB(x - 1, y + 1));
        sum += getColorComponent(ch, color);
        color = new Color(source.getRGB(x + 1, y - 1));
        sum += getColorComponent(ch, color);
        color = new Color(source.getRGB(x + 1, y + 1));
        sum += getColorComponent(ch, color);
        return sum / 4;
    }

    public static int getColorComponent(ColorChoose ch, Color color) {
        return switch (ch) {
            case RED -> color.getRed();
            case GREEN -> color.getGreen();
            case BLUE -> color.getBlue();
        };
    }

    public static void copyEndPixels(final BufferedImage result) {
        for (int x = 0; x < result.getWidth(); x++) {
            result.setRGB(x, 0, result.getRGB(x, 1));
            result.setRGB(x, result.getHeight() - 1, result.getRGB(x, result.getHeight() - 2));
        }
        for (int y = 1; y < result.getHeight() - 1; y++) {
            result.setRGB(0, y, result.getRGB(1, y));
            result.setRGB(result.getWidth() - 1, y, result.getRGB(result.getWidth() - 2, y));
        }
    }

    public static void variable_number_of_gradients(
            final BufferedImage source,
            final BufferedImage result,
            final double koef //коэффициент порогового значения для цвета
    ) {

        /*
                 ↓
        0|0 0|1 0|2 0|3 0|4
        1|0 1|1 1|2 1|3 1|4
      → 2|0 2|1 2|2 2|3 2|4 ←
        3|0 3|1 3|2 3|3 3|4
        4|0 4|1 4|2 4|3 4|4
                 ↑
        */

        for (int x = 2; x < source.getWidth() - 2; x++) {
            for (int y = 2; y < source.getHeight() - 2; y++) {
                variable_number_of_gradients_tack(x, y, source, result, koef, x, y);
            }
        }
        ends(source, result, koef);
    }

    private static void ends(
            final BufferedImage source,
            final BufferedImage result,
            final double koef
    ) {
        Color color;
        for (int x = 2; x < source.getWidth() - 2; x++) {
            for (int y = 0; y < 2; y++) {
                variable_number_of_gradients_tack(x, y, source, result, koef, x, 2);
            }
            for (int y = source.getHeight() - 2; y < source.getHeight(); y++) {
                variable_number_of_gradients_tack(x, y, source, result, koef, x, source.getHeight() - 3);
            }
        }

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < 2; x++) {
                variable_number_of_gradients_tack(x, y, source, result, koef, 2, y);
            }
            for (int x = source.getWidth() - 2; x < source.getWidth(); x++) {
                variable_number_of_gradients_tack(x, y, source, result, koef, source.getWidth() - 3, y);
            }
        }
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                variable_number_of_gradients_tack(x, y, source, result, koef, 2, 2);
                variable_number_of_gradients_tack(x, source.getHeight() - y, source, result, koef, 2, source.getHeight() - 3);
                variable_number_of_gradients_tack(source.getWidth() - x, y, source, result, koef, source.getWidth() - 3, 2);
                variable_number_of_gradients_tack(source.getWidth() - x, source.getHeight() - y, source, result, koef, source.getWidth() - 3, source.getHeight() - 3);
            }
        }
    }

    private static void variable_number_of_gradients_tack(
            int x, int y, BufferedImage source, BufferedImage result,double koef, int xGrad, int yGrad
    ) {
        Color color = new Color(source.getRGB(x, y));
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        double gradR = koef * countGradForColor(x, y, source, ColorChoose.RED);
        double gradG = koef * countGradForColor(x, y, source, ColorChoose.GREEN);
        double gradB = koef * countGradForColor(x, y, source, ColorChoose.BLUE);
        int resR;
        int resG;
        int resB;
        if (red > gradR) {
            resR = (int) Math.round(gradR);
        } else {
            resR = red;
        }
        if (green > gradG) {
            resG = (int) Math.round(gradG);
        } else {
            resG = green;
        }
        if (blue > gradB) {
            resB = (int) Math.round(gradB);
        } else {
            resB = blue;
        }

        Color newColor = new Color(resR, resG, resB);
        result.setRGB(x, y, newColor.getRGB());
    }

    private static int countGradForColor(int x, int y, BufferedImage image, ColorChoose choose) {
        if (x > image.getWidth() - 2 || y > image.getHeight() - 2 || x < 2 || y < 2) {
            throw new IllegalArgumentException("Выход за границы диапозона при подсчёте градиента: x = " + x + " y = " + y);
        }
        int sum = 0;
        sum += getColorComponent(choose, new Color(image.getRGB(x - 2, y - 2)));
        sum += getColorComponent(choose, new Color(image.getRGB(x, y - 2)));
        sum += getColorComponent(choose, new Color(image.getRGB(x + 2, y - 2)));
        sum += getColorComponent(choose, new Color(image.getRGB(x - 2, y)));
        sum += getColorComponent(choose, new Color(image.getRGB(x, y)));
        sum += getColorComponent(choose, new Color(image.getRGB(x + 2, y)));
        sum += getColorComponent(choose, new Color(image.getRGB(x - 2, y + 2)));
        sum += getColorComponent(choose, new Color(image.getRGB(x, y + 2)));
        sum += getColorComponent(choose, new Color(image.getRGB(x + 2, y + 2)));
        return sum;
    }

    public enum ColorChoose {
        RED, GREEN, BLUE
    }
}
