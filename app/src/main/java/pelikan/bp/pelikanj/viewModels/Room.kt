package pelikan.bp.pelikanj.viewModels

data class Room(
    val roomId: Int,
    val name: String,
    val description: String,
    val buildingId: Int,
    val createdAt: String
)