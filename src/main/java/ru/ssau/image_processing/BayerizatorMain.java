package ru.ssau.image_processing;

import ru.ssau.image_processing.utils.Bayerizator;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BayerizatorMain {

    public static void main(String[] args) {
        try {

            if (args.length == 0) {
                System.err.println("Требуется передать минимум 1 аргумент.");
            } else if (args.length == 1) {
                if ("take_photo".equals(args[0])) {
                    PhotoTaker.take();
                    System.out.println("Фотография сделана.");
                } else {
                    System.err.println("Команда не распознана.");
                }
            } else if (args.length == 2) {
                if (!"photo_process".equals(args[0]) || args[1] == null || args[1].isEmpty()) {
                    System.err.println("Переданы неправильные аргументы.");
                    return;
                }
                final String fileName = args[1];
                File file = new File(fileName);
                BufferedImage source = ImageIO.read(file);
                BufferedImage result =
                        new BufferedImage(source.getWidth() / 2, source.getHeight() / 2, BufferedImage.TYPE_INT_RGB);

                Bayerizator.superpixel(source, result);

                File output = new File("out_superpixel.jpg");
                ImageIO.write(result, "jpg", output);
                //
                result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);

                Bayerizator.bilingual_interpolation(source, result);

                File output_1 = new File("out_bilingual_interpolation.jpg");
                ImageIO.write(result, "jpg", output_1);

                result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);

                Bayerizator.variable_number_of_gradients(source, result, 0.8);

                File output_2 = new File("out_variable_number_of_gradients.jpg");
                ImageIO.write(result, "jpg", output_2);
            } else {
                System.err.println("Неправильно задан набор параметров.");
            }

            //Runtime.getRuntime().exec("cmd ./dcraw.exe -c -a -6 -T test.CR2 > mfn.tiff");

        } catch (IOException e) {
            System.out.println("Файл не найден или не удалось сохранить");
        }
    }

    public static void main1(String ... args) {
        try {

            // Открываем изображение
            File file = new File("katana.jpg");
            BufferedImage source = ImageIO.read(file);

            // Создаем новое пустое изображение, такого же размера
            BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());

            // Делаем двойной цикл, чтобы обработать каждый пиксель
            for (int x = 0; x < source.getWidth(); x++) {
                for (int y = 0; y < source.getHeight(); y++) {

                    // Получаем цвет текущего пикселя
                    Color color = new Color(source.getRGB(x, y));

                    // Получаем каналы этого цвета
                    int blue = color.getBlue();
                    int red = color.getRed();
                    int green = color.getGreen();

                    // Применяем стандартный алгоритм для получения черно-белого изображения
                    int grey = (int) (red * 0.299 + green * 0.587 + blue * 0.114);

                    // Если вы понаблюдаете, то заметите что у любого оттенка серого цвета, все каналы имеют
                    // одно и то же значение. Так, как у нас изображение тоже будет состоять из оттенков серого
                    // то, все канали будут иметь одно и то же значение.
                    int newRed = grey;
                    int newGreen = grey;
                    int newBlue = grey;

                    //  Cоздаем новый цвет
                    Color newColor = new Color(newRed, newGreen, newBlue);

                    // И устанавливаем этот цвет в текущий пиксель результирующего изображения
                    result.setRGB(x, y, newColor.getRGB());
                }
            }

            // Созраняем результат в новый файл
            File output = new File("katana_grey.jpg");
            ImageIO.write(result, "jpg", output);

        } catch (IOException e) {

            // При открытии и сохранении файлов, может произойти неожиданный случай.
            // И на этот случай у нас try catch
            System.out.println("Файл не найден или не удалось сохранить");
        }
    }
}
