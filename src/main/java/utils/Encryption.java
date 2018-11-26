package utils;

public final class Encryption {




  public static String encryptDecryptXOR(String rawString) {




    // If encryption is enabled in Config.
    if (Config.getEncryption()) {

      // The key is predefined and hidden in code
      // TODO: Create a more complex code and store it somewhere better - FIXED
      // Har rykket kryptering til Config
      char[] key = Config.getENCYPTION_KEY();

      // Stringbuilder enables you to play around with strings and make useful stuff

      // Stringbuilder bygger en string, som er optimeret til at bygge den i chunks. Append = sætte på bagerst
      // ^binær operation = tager et bogstav og laver om til dens binære værdi.
      StringBuilder thisIsEncrypted = new StringBuilder();

      // TODO: This is where the magic of XOR is happening. Are you able to explain what is going on?

      for (int i = 0; i < rawString.length(); i++) {
        thisIsEncrypted.append((char) (rawString.charAt(i) ^ key[i % key.length]));
      }

      // We return the encrypted string
      return thisIsEncrypted.toString();

    } else {
      // We return without having done anything
      return rawString;
    }
  }
}
