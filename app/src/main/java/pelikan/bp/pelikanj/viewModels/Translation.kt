package pelikan.bp.pelikanj.viewModels

data class Translation(
    val authorUsername: String,
    val createdAt: String,
    val institutionId: Int,
    val isOfficial: Boolean,
    val likesCount: Int,
    val translatedText: String,
    val translationId: Int
)