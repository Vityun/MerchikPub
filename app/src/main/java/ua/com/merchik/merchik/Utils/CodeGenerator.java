package ua.com.merchik.merchik.Utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CodeGenerator {

    public static String generateCode(String secretKey) {
        // Получаем текущую дату в формате ГГГГММДД
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());

        // Комбинируем текущую дату с секретным ключом
        String input = currentDate + secretKey;

        // Хэшируем результат
        String hash = sha256(input);

        // Берем первые 6 цифр из хэша
        return extractDigits(hash, 6);
    }

    private static String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static String extractDigits(String hash, int length) {
        StringBuilder digits = new StringBuilder();
        for (char c : hash.toCharArray()) {
            if (Character.isDigit(c)) {
                digits.append(c);
                if (digits.length() == length) break;
            }
        }
        return digits.toString();
    }



    public static void main(String[] args) {
        String secretKey = "SecretKey"; // Замените на свой секретный ключ
        String code = generateCode(secretKey);
        System.out.println("Generated Code: " + code);
    }

    public static String getCode(){
        String secretKey = "SecretKey"; // Замените на свой секретный ключ
        return generateCode(secretKey);
    }
}
