package co.esclub.searchnshop.adapter

/**
 * Created by tae.kim on 28/06/2017.
 */

interface ItemTouchHelperAdapter {
    fun onItemMove(fromPosition: Int, toPosition: Int)
    fun onItemDismiss(position: Int)
}