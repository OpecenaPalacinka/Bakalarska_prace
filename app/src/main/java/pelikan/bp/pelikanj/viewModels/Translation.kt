package pelikan.bp.pelikanj.viewModels

data class Translation(
    val translationId: Int,
    val institutionId: Int,
    val authorUsername: String,
    val translatedText: String,
    val isOfficial: Boolean,
    val createdAt: String,
    val likesCount: Int
)