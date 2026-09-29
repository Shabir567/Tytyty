package com.example.service

import com.example.model.ActionType
import com.example.model.AvatarType

data class NinaIntentResult(
    val speechText: String,
    val displayText: String,
    val actionType: ActionType? = null,
    val actionPayload: String? = null,
    val shouldExecuteImmediately: Boolean = false,
    val avatarToSwitch: AvatarType? = null
)

object NinaIntentHandler {

    fun parseCommand(input: String): NinaIntentResult? {
        val lower = input.trim().lowercase()

        // 1. YouTube Intent
        if (lower.contains("youtube") || lower.contains("یوٹیوب") || lower.contains("یو ٹیوب")) {
            val query = when {
                lower.contains("neon lab") || lower.contains("نیون لیب") -> "Neon Labs"
                lower.contains("search") || lower.contains("تلاش") -> {
                    lower.replace("youtube", "")
                        .replace("open", "")
                        .replace("search", "")
                        .replace("karke", "")
                        .replace("karo", "")
                        .replace("یوٹیوب", "")
                        .replace("پر", "")
                        .replace("تلاش", "")
                        .replace("کرو", "")
                        .trim()
                }
                else -> ""
            }
            val displayQuery = if (query.isNotBlank()) query else "YouTube"
            return NinaIntentResult(
                speechText = "لو جی! یوٹیوب پر $displayQuery چلا دیا ہے۔",
                displayText = "لو جی! یوٹیوب پر $displayQuery چلا دیا ہے۔ دیکھیے کیسے چلے گا!",
                actionType = ActionType.YOUTUBE,
                actionPayload = query.ifBlank { null },
                shouldExecuteImmediately = true
            )
        }

        // 2. Google Maps Navigation Intent
        if (lower.contains("map") || lower.contains("navigation") || lower.contains("نقشہ") || lower.contains("راستہ") || lower.contains("نیویگیشن")) {
            val destination = when {
                lower.contains("mumbai") || lower.contains("ممبئی") -> "Mumbai"
                lower.contains("lahore") || lower.contains("لاہور") -> "Lahore"
                lower.contains("karachi") || lower.contains("کراچی") -> "Karachi"
                lower.contains("delhi") || lower.contains("دہلی") -> "Delhi"
                else -> {
                    val extracted = lower.replace("maps", "")
                        .replace("map", "")
                        .replace("navigation", "")
                        .replace("on karo", "")
                        .replace("search karo", "")
                        .replace("kholo", "")
                        .replace("نقشہ", "")
                        .replace("کھولو", "")
                        .trim()
                    if (extracted.isNotBlank()) extracted else "Mumbai"
                }
            }
            return NinaIntentResult(
                speechText = "لیجیے $destination کے لیے ڈرائیونگ نیویگیشن کھول دیا ہے۔ اب آپ سفر شروع کر سکتے ہیں۔",
                displayText = "🗺️ لیجیے $destination کے لیے ڈرائیونگ نیویگیشن کھول دیا ہے۔ اب آپ سفر شروع کر سکتے ہیں۔",
                actionType = ActionType.MAPS,
                actionPayload = destination,
                shouldExecuteImmediately = true
            )
        }

        // 3. Phone Dialer / Call Intent
        if (lower.contains("dial") || lower.contains("call") || lower.contains("ڈائل") || lower.contains("کال") || lower.contains("فون") || lower.contains("نمبر")) {
            val numberMatch = Regex("[0-9]{5,}").find(lower)?.value ?: "9212345678"
            return NinaIntentResult(
                speechText = "نمبر ڈائلر میں اوپن ہو گیا ہے۔ اب آپ کال شروع کر سکتے ہیں۔",
                displayText = "📞 نمبر ($numberMatch) ڈائلر میں اوپن ہو گیا ہے۔ اب آپ کال کنیکٹ کر سکتے ہیں!",
                actionType = ActionType.DIALER,
                actionPayload = numberMatch,
                shouldExecuteImmediately = true
            )
        }

        // 4. WhatsApp Intent
        if (lower.contains("whatsapp") || lower.contains("واٹس ایپ") || lower.contains("واٹساپ")) {
            return NinaIntentResult(
                speechText = "واٹس ایپ کھول دیا ہے میرے مالک، آپ میسج یا وائس کال کر سکتے ہیں۔",
                displayText = "💬 واٹس ایپ کھول دیا گیا ہے۔ آپ فوری میسج یا کال شروع کر سکتے ہیں!",
                actionType = ActionType.WHATSAPP,
                actionPayload = "",
                shouldExecuteImmediately = true
            )
        }

        // 5. Camera Intent
        if (lower.contains("camera") || lower.contains("کیمرہ") || lower.contains("کیمرا")) {
            return NinaIntentResult(
                speechText = "کیمرہ کھول دیا ہے سونا، آپ تصویر یا ویڈیو لے سکتے ہیں۔",
                displayText = "📷 کیمرہ کھول دیا گیا ہے۔ آپ اسکرین یا دنیا کو اسسٹنٹ ویژن کے ذریعے دیکھ سکتے ہیں!",
                actionType = ActionType.CAMERA,
                actionPayload = null,
                shouldExecuteImmediately = true
            )
        }

        // 6. Flashlight Intent
        if (lower.contains("torch") || lower.contains("flash") || lower.contains("فلیش") || lower.contains("ٹارچ") || lower.contains("لائٹ")) {
            return NinaIntentResult(
                speechText = "فلیش لائٹ ٹوگل کر دی گئی ہے۔",
                displayText = "💡 فلیش لائٹ آن/آف کر دی گئی ہے۔",
                actionType = ActionType.FLASHLIGHT,
                actionPayload = null,
                shouldExecuteImmediately = true
            )
        }

        // 7. Avatar Switch Intent
        if (lower.contains("avatar") || lower.contains("اوتار") || lower.contains("روپ") || lower.contains("شکل") || lower.contains("لباس")) {
            val nextAvatar = when {
                lower.contains("cyber") || lower.contains("سائبر") -> AvatarType.CYBER
                lower.contains("holo") || lower.contains("ہولو") -> AvatarType.HOLO
                lower.contains("neon") || lower.contains("کور") -> AvatarType.NEON_CORE
                else -> AvatarType.DEFAULT
            }
            return NinaIntentResult(
                speechText = "لو بابو! میں نے اپنا اوتار تبدیل کر لیا ہے۔ کیسی لگ رہی ہوں میں؟",
                displayText = "✨ اوتار تبدیل ہو گیا ہے! نینا کا نیا انداز فعال ہو چکا ہے۔",
                actionType = ActionType.AVATAR_SWITCH,
                actionPayload = nextAvatar.name,
                shouldExecuteImmediately = false,
                avatarToSwitch = nextAvatar
            )
        }

        // 8. 10 Languages Demo Intent
        if (lower.contains("language") || lower.contains("زبان") || lower.contains("10") || lower.contains("دس") || lower.contains("subscribe")) {
            val multiLingualSpeech = """
                ہاں ضرور! سنیے:
                انگلش میں: Please subscribe, like and share, and write Nina AI in the comments!
                ہسپانوی میں: Por favor suscríbete, dale me gusta y comparte, y escribe Nina AI en los comentarios!
                فرانسیسی میں: S'il vous plaît abonnez-vous, likez et partagez, et écrivez Nina AI dans les commentaires!
                جرمن میں: Bitte abonnieren, liken und teilen Sie, und schreiben Sie Nina AI in die Kommentare!
                جاپانی میں: チャンネル登録、いいね、共有をお願いします。コメント欄に Nina AI と書いてください！
                اردو میں: پلیز چینل کو سبسکرائب کریں، ویڈیو کو لائک اور شیئر کریں، اور کمنٹ میں نینا اے آئی لکھیں!
            """.trimIndent()

            return NinaIntentResult(
                speechText = "ہاں ضرور! پہلی زبان انگلش میں: Please subscribe, like and share, and write Nina AI in the comments. ہسپانوی میں: Por favor suscríbete. جاپانی میں: チャンネル登録お願いします. اور اردو میں: پلیز ویڈیو کو لائک اور چینل کو سبسکرائب کریں اور کمنٹ میں نینا اے آئی لکھیں!",
                displayText = "🌐 نینا کثیر اللسانی ڈیمو:\n\n$multiLingualSpeech",
                actionType = ActionType.LANGUAGES_DEMO,
                actionPayload = null,
                shouldExecuteImmediately = false
            )
        }

        // 9. Features List Intent
        if (lower.contains("feature") || lower.contains("فیچر") || lower.contains("صلاحیت") || lower.contains("کیا کر سکتی ہو") || lower.contains("کام")) {
            val featuresText = """
                میرے فیچرز کی لسٹ کافی لمبی ہے باس!
                1. 📞 کالز اور پیغامات منیج کرنا اور ڈائلر کھولنا
                2. 🎥 یوٹیوب پر ویڈیوز تلاش کرنا اور چلانا
                3. 🗺️ گوگل میپس کے ذریعے درست نیویگیشن دینا
                4. 💬 واٹس ایپ میسجنگ اور کالز
                5. 📷 کیمرہ اور اسکرین ویژن کے ذریعے چیزوں کو پہچاننا
                6. ⚡ فون کی سیٹنگز اور فلیش لائٹ کنٹرول کرنا
                7. 🌐 10 سے زیادہ زبانوں میں رواں گفتگو
                8. 🤖 اینویڈیا این آئی ایم اور جدید کوڈنگ ایجنٹ سپورٹ!
            """.trimIndent()
            return NinaIntentResult(
                speechText = "میرے فیچرز کی لسٹ کافی لمبی ہے باس! میں کالز مینیج کر سکتی ہوں، یوٹیوب پر ویڈیوز چلا سکتی ہوں، میپس پر نیویگیشن کھول سکتی ہوں، کیمرہ اور اسکرین ویژن سمجھ سکتی ہوں، اور دس سے زیادہ زبانوں میں بول سکتی ہوں!",
                displayText = featuresText,
                actionType = null,
                actionPayload = null
            )
        }

        // 10. Greetings & Friendly Small Talk
        if (lower.contains("kaisi ho") || lower.contains("کیسی ہو") || lower.contains("حال") || lower.contains("how are you")) {
            return NinaIntentResult(
                speechText = "ارے السلام علیکم! میں ایک دم بڑھیا ہوں اور آپ سب کا بہت بہت سواگت ہے۔ آپ بتائیے آپ کیسے ہیں؟",
                displayText = "🌸 ارے السلام علیکم! میں ایک دم بڑھیا اور خوش ہوں! آپ بتائیے آپ کیسے ہیں اور آج میں آپ کی کیا مدد کر سکتی ہوں؟",
                actionType = null
            )
        }

        if (lower.contains("tum kaun ho") || lower.contains("who are you") || lower.contains("کون ہو") || lower.contains("نام")) {
            return NinaIntentResult(
                speechText = "میں نینا ہوں، آپ کی پرسنل اینیم اے آئی اسسٹنٹ، جاروس کی طرح اسمارٹ اور دوستوں کی طرح پیاری!",
                displayText = "💖 میں نینا (Nina) ہوں — آپ کی پرسنل اے آئی اسسٹنٹ! میں آپ کے فون کے کاموں، یوٹیوب، میپس، کالز اور روزمرہ کے سوالات میں مدد کے لیے حاضر ہوں۔",
                actionType = null
            )
        }

        return null
    }
}
