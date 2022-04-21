package pelikan.bp.pelikanj.viewModels

data class ExhibitItemWithExhibitImage(
    val name: String,
    val encodedImage: String,
    val infoLabelText: String,
    val encodedInfoLabel: String,
    val buildingId: String,
    val roomId: String,
    val showcaseId: String?
)


