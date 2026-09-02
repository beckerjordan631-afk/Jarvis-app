package com.jarvis.assistant.skills

import com.jarvis.assistant.data.AppDatabase

/**
 * Minimal safe arithmetic evaluator (+ - * / ^, parentheses) — no
 * external library, no reflection, no eval of arbitrary code.
 */
class CalculatorSkill : Skill {
    override val id = "calculator"

    override fun matches(input: String): Boolean {
        val trigger = input.contains("rechne") || input.contains("berechne") ||
            input.contains("was ist") && containsMathOperator(input)
        return trigger && containsDigit(input)
    }

    private fun containsDigit(s: String) = s.any { it.isDigit() }
    private fun containsMathOperator(s: String) =
        listOf("+", "-", "*", "x", "mal", "plus", "minus", "geteilt", "durch", "/").any { s.contains(it) }

    override suspend fun execute(input: String, db: AppDatabase): SkillResult {
        val expr = normalize(input)
        return try {
            val result = Evaluator(expr).evaluate()
            val formatted = if (result == result.toLong().toDouble()) result.toLong().toString()
                else result.toString()
            SkillResult("Das Ergebnis ist $formatted.")
        } catch (e: Exception) {
            SkillResult("Das konnte ich nicht berechnen. Sag mir die Rechnung z.B. so: 'Rechne 12 mal 4'.")
        }
    }

    private fun normalize(input: String): String {
        var s = input.lowercase()
        listOf("rechne", "berechne", "was ist", "jarvis").forEach { s = s.replace(it, "") }
        s = s.replace("mal", "*").replace(" x ", "*")
            .replace("plus", "+")
            .replace("minus", "-")
            .replace("geteilt durch", "/").replace("durch", "/")
            .replace(",", ".")
        return s.filter { it.isDigit() || it in "+-*/(). " }
    }

    /** Tiny recursive-descent parser for +,-,*,/,(,) and decimals. */
    private class Evaluator(private val text: String) {
        private var pos = 0
        fun evaluate(): Double {
            val v = parseExpr()
            return v
        }
        private fun peek(): Char? = if (pos < text.length) text[pos] else null
        private fun skipSpaces() { while (peek() == ' ') pos++ }

        private fun parseExpr(): Double {
            skipSpaces()
            var value = parseTerm()
            while (true) {
                skipSpaces()
                when (peek()) {
                    '+' -> { pos++; value += parseTerm() }
                    '-' -> { pos++; value -= parseTerm() }
                    else -> return value
                }
            }
        }

        private fun parseTerm(): Double {
            skipSpaces()
            var value = parseFactor()
            while (true) {
                skipSpaces()
                when (peek()) {
                    '*' -> { pos++; value *= parseFactor() }
                    '/' -> { pos++; value /= parseFactor() }
                    else -> return value
                }
            }
        }

        private fun parseFactor(): Double {
            skipSpaces()
            if (peek() == '(') {
                pos++
                val v = parseExpr()
                skipSpaces()
                if (peek() == ')') pos++
                return v
            }
            if (peek() == '-') { pos++; return -parseFactor() }
            val start = pos
            while (peek() != null && (peek()!!.isDigit() || peek() == '.')) pos++
            if (start == pos) throw IllegalArgumentException("Unexpected char")
            return text.substring(start, pos).toDouble()
        }
    }
}
