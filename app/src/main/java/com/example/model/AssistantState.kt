package com.example.model

import androidx.annotation.DrawableRes
import com.example.R
import java.util.UUID

enum class AssistantStatus(val labelUrdu: String, val labelEn: String) {
    IDLE("تیار ہے", "Ready"),
    LISTENING("سن رہی ہوں...", "Listening..."),
    PROCESSING("سوچ رہی ہوں...", "Thinking..."),
    SPEAKING("بول رہی ہوں...", "Speaking...")
}

enum class AvatarType(
    val titleUrdu: String,
    val titleEn: String,
    val subtitle: String,
    @DrawableRes val drawableRes: Int?
) {
    DEFAULT("نینا کلاسک", "Nina Classic", "دوستانہ اور پیاری اینیم اسسٹنٹ", R.drawable.ic_nina_avatar_default),
    CYBER("سائبر نینا", "Cyber Nina", "جدید ہائی ٹیک باڈی سوٹ", R.drawable.ic_nina_avatar_cyber),
    HOLO("ہولوگرام نینا", "Hologram Nina", "لیزر ڈیجیٹل اسسٹنٹ", R.drawable.ic_nina_avatar_holo),
    NEON_CORE("نیون کور", "Neon Core", "جاروس طرز کا ایکٹو ری ایکٹر", null)
}

enum class AppLanguage(val nativeName: String, val code: String, val greeting: String) {
    URDU("اردو", "ur", "السلام علیکم! میں نینا ہوں، آپ کی ذاتی اے آئی اسسٹنٹ۔ میں آپ کی کیا مدد کر سکتی ہوں؟"),
    HINDI("हिन्दी", "hi", "नमस्ते! मैं नीना हूँ, आपकी पर्सनल एआई असिस्टेंट। मैं आपकी क्या मदद कर सकती हूँ?"),
    ENGLISH("English", "en", "Hello! I am Nina, your personal AI assistant. How can I help you today?"),
    SPANISH("Español", "es", "¡Hola! Soy Nina, tu asistente personal de IA. ¿Cómo puedo ayudarte?"),
    FRENCH("Français", "fr", "Bonjour! Je suis Nina, votre assistante IA personnelle. Que puis-je faire pour vous?"),
    GERMAN("Deutsch", "de", "Hallo! Ich bin Nina, Ihre persönliche KI-Assistentin. Wie kann ich Ihnen helfen?"),
    JAPANESE("日本語", "ja", "こんにちは！私はパーソナルAIアシスタントのニーナです。何をお手伝いしましょうか？"),
    KOREAN("한국어", "ko", "안녕하세요! 저는 당신의 개인 AI 비서 니나입니다. 무엇을 도와드릴까요?"),
    RUSSIAN("Русский", "ru", "Привет! Я Нина, ваш персональный ИИ-ассистент. Чем я могу помочь?")
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: ActionType? = null,
    val actionPayload: String? = null
)

enum class ActionType(val title: String) {
    YOUTUBE("YouTube"),
    MAPS("Google Maps"),
    DIALER("Phone Call"),
    WHATSAPP("WhatsApp"),
    CAMERA("Camera"),
    FLASHLIGHT("Flashlight"),
    LANGUAGES_DEMO("10 Languages"),
    AVATAR_SWITCH("Avatar Switch")
}

data class AgentModule(
    val id: String,
    val name: String,
    val urduName: String,
    val description: String,
    val isEnabled: Boolean = true,
    val category: String = "AI Core"
)
