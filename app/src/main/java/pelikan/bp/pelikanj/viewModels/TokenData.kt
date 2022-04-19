package pelikan.bp.pelikanj.viewModels

data class TokenData(
    val createdAt: String,
    val email: String,
    val exp: Int,
    val iat: Int,
    val id: String,
    val isAdmin: Boolean,
    val isInstitutionOwner: Boolean,
    val isTranslator: Boolean,
    val username: String
)