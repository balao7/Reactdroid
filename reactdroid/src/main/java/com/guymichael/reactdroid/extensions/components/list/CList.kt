package com.guymichael.reactdroid.extensions.components.list

import android.app.Activity
import android.view.View
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import com.guymichael.reactdroid.extensions.components.list.adapter.model.RecyclerComponentViewHolder
import com.guymichael.reactdroid.extensions.components.list.adapter.RecyclerComponentAdapter
import com.guymichael.reactdroid.extensions.components.list.model.ListItemProps
import com.guymichael.reactdroid.core.model.AComponent
import com.guymichael.reactdroid.extensions.components.list.model.ListProps
import com.guymichael.reactdroid.extensions.components.list.model.ListState

class CList(
        v: RecyclerView
        , override val adapter: RecyclerComponentAdapter
    ) : BaseListComponent<ListProps, ListState, RecyclerView>(v) {

    @JvmOverloads
    constructor(v: RecyclerView, orientation: Int = RecyclerView.VERTICAL)
    : this(v, createAdapter(v, orientation))


    /* lifecycle */

    override fun createInitialState(props: ListProps) = ListState(props.uncontrolled_initialScrollIndex)

    override fun componentDidMount() {
        //if we received scroll position through props (initial or controlled),
        if (getScrollIndex() != null) {
            // listen on scroll state changes to update state's scrollIndex (align with actual - adapter)
            listenOnScrollStateChanges()
        }
    }

    override fun componentDidUpdate(prevProps: ListProps, prevState: ListState, snapshot: Any?) {
        // "listen" on data-size changes to update state's scrollIndex (align with actual - adapter)
        // (scrollListener isn't invoked when data changes, sadly)
        if (prevProps.items.size != this.props.items.size) {
            alignOwnStateScrollWithActualIfNeeded()
        }
    }




    /* API */
    fun onRender(items: List<ListItemProps>) {
        onRender(ListProps(items))
    }




    /* privates */

    private fun getScrollIndex(props: ListProps = this.props, state: ListState = this.ownState): Int? {
        //controlled scroll mode
        return props.controlledScroll?.first

        //or uncontrolled
        ?: state.uncontrolledScrollIndex
    }

    private fun listenOnScrollStateChanges() {
        adapter.addOnListScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    onListScrollIndexChanged(adapter.getActualFirstVisiblePosition())
                }
            }
        })
    }

    private fun alignOwnStateScrollWithActualIfNeeded() {
        getScrollIndex()?.takeIf { adapter.itemCount > 0 }?.also { stateIndex ->
        adapter.getActualFirstVisiblePosition().takeIf { it >= 0 && it != stateIndex }
            ?.let { actualIndex ->

            setState(ownState.cloneWithNewScroll(actualIndex))
        }}
    }



    /* callbacks */

    //called only when props provide scroll index (controlled or not)
    private fun onListScrollIndexChanged(newIndex: Int) {
        //controlled scroll mode
        this.props.controlledScroll?.second?.also {
            it.invoke(newIndex)
        }

        //or uncontrolled
        ?: this.setState(ListState(newIndex))
    }





    /* render */

    private fun renderAdapterScrollPosition(index: Int, smoothScroll: Boolean) {
        if (smoothScroll) {
            adapter.smoothScroll(index)
        } else {
            adapter.scrollImmediately(index)
        }
    }

    private var didFirstRenderScrollPosition = false //for first scroll (immediate) or latter (smooth)
    private fun renderScrollPosition(scrollIndex: Int) {
        if (adapter.itemCount > 0) {
            if (adapter.getActualFirstVisiblePosition() != scrollIndex) {
                renderAdapterScrollPosition(scrollIndex, didFirstRenderScrollPosition)
            }

            //this is considered the first scroll-render with non-empty list
            didFirstRenderScrollPosition = true
        }
    }

    private var didFirstRender = false
    override fun render() {
        var dataSizeChanged = false

        if( !didFirstRender || adapter.getAllItems() != props.items) { //THINK efficiency
            dataSizeChanged = didFirstRender && adapter.itemCount != props.items.size
            //notify data set changed
            super.render()
        }

        //update adapter's scroll position
        if( !dataSizeChanged) {
            getScrollIndex()?.also(::renderScrollPosition)
        } //if data sized did change, componentDidUpdate will setState again to align scrollIndex,
          // which calls re-render (if needed) in which case dataSizeChanged will be false..
          // Note: we do it like this because scrolling while the data is changing is a bad idea:
          // The actual views doesn't update immediately and we don't currently know a good way to
          // listen to actual 'child view changes in recycler'.
          // There are two flows in mind:
          // 1. If the scroll comes from ownState (uncontrolled), didUpdate will reset it to adapter's pos
          //    in which case the re-render will be a no op (instead of scrolling the adapter will data changes)
          // 2. If the scroll comes from the props, didUpdate will reset the ownState,
          //    then re-render (--> renderScrollPosition) will see that getScrollIndex() != actual
          //    (because getScrollIndex gives props' index precedence!) and scroll the adapter

        didFirstRender = true
    }
}











private fun createAdapter(v: RecyclerView, orientation: Int = RecyclerView.VERTICAL): RecyclerComponentAdapter {
    return RecyclerComponentAdapter(v
        , viewHolderSupplier = ::RecyclerComponentViewHolder
        , orientation = orientation)
}

//THINK as Annotations
//export as a method
fun withList(recycler: RecyclerView, orientation: Int = RecyclerView.VERTICAL)
    = CList(recycler, orientation)

fun View.withList(@IdRes id: Int, orientation: Int = RecyclerView.VERTICAL)
    = CList(findViewById(id), orientation)

fun AComponent<*, *, *>.withList(@IdRes id: Int, orientation: Int = RecyclerView.VERTICAL)
    = CList(mView.findViewById(id), orientation)

fun Activity.withList(@IdRes id: Int, orientation: Int = RecyclerView.VERTICAL)
    = CList(findViewById(id), orientation)