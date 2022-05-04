package pelikan.bp.pelikanj.ui.more.languageSetting

import pelikan.bp.pelikanj.viewModels.MyLanguages

interface OnSelectListener {
    /**
     * On item clicked
     *
     * @param myLanguages long and short name of language
     */
    fun onItemClicked(myLanguages: MyLanguages)
}