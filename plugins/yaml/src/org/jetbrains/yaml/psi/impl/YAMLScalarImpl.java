package org.jetbrains.yaml.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLScalar;

import java.util.List;

public abstract class YAMLScalarImpl extends YAMLValueImpl implements YAMLScalar {
  public YAMLScalarImpl(@NotNull ASTNode node) {
    super(node);
  }

  @NotNull
  public abstract List<TextRange> getContentRanges();

  @NotNull
  protected abstract String getRangesJoiner(@NotNull CharSequence leftString, @NotNull CharSequence rightString);

  @NotNull
  @Override
  public String getTextValue() {
    final String text = getText();


    final StringBuilder builder = new StringBuilder();
    CharSequence prevString = null;
    boolean isFirst = true;
    for (TextRange range : getContentRanges()) {
      final CharSequence curString = range.subSequence(text);

      if (!isFirst) {
        builder.append(getRangesJoiner(prevString, curString));
      }
      else {
        isFirst = false;
      }
      builder.append(curString);
      prevString = curString;
    }
    return builder.toString();
  }
}
