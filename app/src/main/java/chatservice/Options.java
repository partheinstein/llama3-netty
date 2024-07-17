package chatservice;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.*;

public record Options(
    Path modelPath,
    String prompt,
    String systemPrompt,
    boolean interactive,
    float temperature,
    float topp,
    long seed,
    int maxTokens,
    boolean stream,
    boolean echo,
    ByteArrayOutputStream out) {

  public Options {
    require(modelPath != null, "Missing argument: --model <path> is required");
    require(
        interactive || prompt != null,
        "Missing argument: --prompt is required in --instruct mode e.g. --prompt \"Why is the sky blue?\"");
    require(0 <= temperature, "Invalid argument: --temperature must be non-negative");
    require(0 <= topp && topp <= 1, "Invalid argument: --top-p must be within [0, 1]");
  }

  static void require(boolean condition, String messageFormat, Object... args) {
    if (!condition) {
      System.out.println("ERROR " + messageFormat.formatted(args));
      System.out.println();
      throw new IllegalArgumentException();
    }
  }
}
