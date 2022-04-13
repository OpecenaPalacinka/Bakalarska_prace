package pelikan.bp.pelikanj.viewModels

data class ExhibitModelItem(
    val building: Building,
    val createdAt: String,
    val exhibitId: Int,
    val image: String,
    val infoLabel: String,
    val infoLabelText: String,
    val name: String,
    val room: Room,
    val showcase: Showcase
)