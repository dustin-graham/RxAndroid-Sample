package com.example.rxandroid;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * adapted from: https://gist.github.com/gabrielemariotti/4c189fb1124df4556058
 */
public class SectionedRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private static final int SECTION_TYPE = 0;

    private boolean mValid = true;
    private RecyclerView.Adapter mBaseAdapter;
    private SectionDelegate sectionDelegate;

    public  interface SectionDelegate<T extends RecyclerView.ViewHolder>  {
        int getSectionCount();
        int getRowsInSection(int sectionIndex);
        T createSectionViewHolder(ViewGroup parent);
        void onBindSectionViewHolder(T sectionViewHolder,int sectionPosition);
    }

    public SectionedRecyclerViewAdapter(Context context, RecyclerView.Adapter baseAdapter, SectionDelegate sectionDelegate) {
        this.sectionDelegate = sectionDelegate;

        mBaseAdapter = baseAdapter;
        mContext = context;

        mBaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                mValid = mBaseAdapter.getItemCount()>0;
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                mValid = mBaseAdapter.getItemCount()>0;
                notifyItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mValid = mBaseAdapter.getItemCount()>0;
                notifyItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                mValid = mBaseAdapter.getItemCount()>0;
                notifyItemRangeRemoved(positionStart, itemCount);
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int typeView) {
        if (typeView == SECTION_TYPE) {
            return sectionDelegate.createSectionViewHolder(parent);
        }else{
            return mBaseAdapter.onCreateViewHolder(parent, typeView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder sectionViewHolder, int position) {
        if (isSectionHeaderPosition(position, mBaseAdapter.getItemCount(), sectionDelegate)) {
            sectionDelegate.onBindSectionViewHolder(sectionViewHolder,positionToSectionedPosition(position, mBaseAdapter.getItemCount(), sectionDelegate));
        }else{
            mBaseAdapter.onBindViewHolder(sectionViewHolder,sectionedPositionToPosition(position, mBaseAdapter.getItemCount(), sectionDelegate));
        }

    }

    @Override
    public int getItemViewType(int position) {
        return isSectionHeaderPosition(position, mBaseAdapter.getItemCount(), sectionDelegate)
                ? SECTION_TYPE
                : mBaseAdapter.getItemViewType(sectionedPositionToPosition(position, mBaseAdapter.getItemCount(), sectionDelegate)) +1 ;
    }

    public static class Section {
        int firstPosition;
        int sectionedPosition;
        CharSequence title;

        public Section(int firstPosition, CharSequence title) {
            this.firstPosition = firstPosition;
            this.title = title;
        }

        public CharSequence getTitle() {
            return title;
        }
    }

    public static int positionToSectionedPosition(int position, int itemCount, SectionDelegate delegate) {
        if (position < 0 || position >= itemCount) {
            return -1;
        }
        int section = positionToSectionIndex(position, delegate);
        return section + 1 + position;
    }

    public static int positionToSectionIndex(int position, SectionDelegate delegate) {
        int currentSection = 0;
        int cumulativeRows = 0;
        while (currentSection < delegate.getSectionCount()) {
            cumulativeRows += delegate.getRowsInSection(currentSection);
            if (cumulativeRows-1 >= position) {
                break;
            }
            ++currentSection;
        }
        return currentSection;
    }

    public static int sectionedPositionToPosition(int sectionedPosition, int itemCount, SectionDelegate delegate) {
        if (sectionedPosition == 0) {
            return RecyclerView.NO_POSITION;
        }

        int offset = 0;
        int currentSection = 0;
        int cumulativeRows = 0;
        while (currentSection < delegate.getSectionCount()) {
            cumulativeRows += delegate.getRowsInSection(currentSection);
            ++offset;
            if (cumulativeRows + offset == sectionedPosition) {
                return RecyclerView.NO_POSITION;
            }
            if ((cumulativeRows+offset)-1 >= sectionedPosition) {
                break;
            }
            ++currentSection;
        }
        return sectionedPosition - offset;
    }

    public static boolean isSectionHeaderPosition(int position, int itemCount, SectionDelegate delegate) {
        return position >= 0 && position < itemCount && sectionedPositionToPosition(position, itemCount, delegate) == RecyclerView.NO_POSITION;
    }

    public static IndexPath positionToIndexPath(int position, SectionDelegate delegate) {
        int section = positionToSectionIndex(position, delegate);
        int allPreviousRows = 0;
        int currentSection = section-1;
        while (currentSection > 0) {
            allPreviousRows += delegate.getRowsInSection(currentSection);
        }
        int rowInSection = position - allPreviousRows;
        return new IndexPath(section, rowInSection);
    }

    public static class IndexPath {
        public int sectionIndex;
        public int rowIndex;

        public IndexPath(int sectionIndex, int rowIndex) {
            this.sectionIndex = sectionIndex;
            this.rowIndex = rowIndex;
        }
    }

    @Override
    public long getItemId(int position) {
        return isSectionHeaderPosition(position, mBaseAdapter.getItemCount(), sectionDelegate)
                ? Integer.MAX_VALUE - positionToSectionIndex(position, sectionDelegate)
                : mBaseAdapter.getItemId(sectionedPositionToPosition(position, mBaseAdapter.getItemCount(), sectionDelegate));
    }

    @Override
    public int getItemCount() {
        return (mValid ? mBaseAdapter.getItemCount() + sectionDelegate.getSectionCount() : 0);
    }

}
