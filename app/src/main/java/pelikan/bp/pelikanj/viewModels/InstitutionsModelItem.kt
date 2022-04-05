package pelikan.bp.pelikanj.viewModels

data class InstitutionsModelItem(
    val address: String,
    val createdAt: String,
    val image: String,
    val institutionId: Int,
    val latitude: Double,
    val longitude: Double,
    val name: String
)