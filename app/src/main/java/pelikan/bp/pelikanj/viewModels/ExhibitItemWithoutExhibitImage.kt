package pelikan.bp.pelikanj.viewModels

data class ExhibitItemWithoutExhibitImage(
    val name: String,
    val infoLabelText: String,
    val encodedInfoLabel: String,
    val buildingId: String,
    val roomId: String,
    val showcaseId: String
)
