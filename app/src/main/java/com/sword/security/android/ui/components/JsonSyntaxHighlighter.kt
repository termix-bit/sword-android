package com.sword.security.android.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

data class JsonHighlightPalette(
    val keyColor: Color,
    val stringColor: Color,
    val numberColor: Color,
    val booleanColor: Color,
    val punctuationColor: Color,
    val fallbackColor: Color
)

object JsonSyntaxHighlighter {

    fun highlight(
        jsonText: String,
        palette: JsonHighlightPalette,
        maxChars: Int = 150_000
    ): AnnotatedString {
        val text = if (jsonText.length > maxChars) {
            jsonText.substring(0, maxChars) + "\n... [truncated for display; copy or export for full content]"
        } else {
            jsonText
        }

        return buildAnnotatedString {
            var i = 0
            val n = text.length
            while (i < n) {
                val ch = text[i]
                when {
                    ch == '"' -> {
                        val start = i
                        i++
                        while (i < n) {
                            val c = text[i]
                            if (c == '\\' && i + 1 < n) {
                                i += 2
                                continue
                            }
                            if (c == '"') {
                                i++
                                break
                            }
                            i++
                        }
                        val token = text.substring(start, i)
                        var lookahead = i
                        while (lookahead < n && text[lookahead].isWhitespace()) {
                            lookahead++
                        }
                        val isKey = lookahead < n && text[lookahead] == ':'
                        withStyle(
                            SpanStyle(
                                color = if (isKey) palette.keyColor else palette.stringColor,
                                fontWeight = if (isKey) FontWeight.SemiBold else FontWeight.Normal
                            )
                        ) {
                            append(token)
                        }
                    }

                    ch == '{' || ch == '}' || ch == '[' || ch == ']' || ch == ':' || ch == ',' -> {
                        withStyle(SpanStyle(color = palette.punctuationColor, fontWeight = FontWeight.Bold)) {
                            append(ch)
                        }
                        i++
                    }

                    ch == '-' || ch.isDigit() -> {
                        val start = i
                        i++
                        while (i < n && (text[i].isDigit() || text[i] in ".eE+-")) {
                            i++
                        }
                        withStyle(SpanStyle(color = palette.numberColor)) {
                            append(text.substring(start, i))
                        }
                    }

                    text.startsWith("true", startIndex = i) && isWordBoundary(text, i - 1) && isWordBoundary(text, i + 4) -> {
                        withStyle(SpanStyle(color = palette.booleanColor, fontWeight = FontWeight.SemiBold)) {
                            append("true")
                        }
                        i += 4
                    }

                    text.startsWith("false", startIndex = i) && isWordBoundary(text, i - 1) && isWordBoundary(text, i + 5) -> {
                        withStyle(SpanStyle(color = palette.booleanColor, fontWeight = FontWeight.SemiBold)) {
                            append("false")
                        }
                        i += 5
                    }

                    text.startsWith("null", startIndex = i) && isWordBoundary(text, i - 1) && isWordBoundary(text, i + 4) -> {
                        withStyle(SpanStyle(color = palette.booleanColor, fontWeight = FontWeight.SemiBold)) {
                            append("null")
                        }
                        i += 4
                    }

                    ch.isWhitespace() -> {
                        append(ch)
                        i++
                    }

                    else -> {
                        withStyle(SpanStyle(color = palette.fallbackColor)) {
                            append(ch)
                        }
                        i++
                    }
                }
            }
        }
    }

    private fun isWordBoundary(text: String, index: Int): Boolean {
        if (index < 0 || index >= text.length) return true
        val c = text[index]
        return !c.isLetterOrDigit() && c != '_'
    }
}
