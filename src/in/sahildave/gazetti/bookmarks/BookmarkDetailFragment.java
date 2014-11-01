package in.sahildave.gazetti.bookmarks;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.crashlytics.android.Crashlytics;
import in.sahildave.gazetti.R;
import in.sahildave.gazetti.bookmarks.sqlite.BookmarkDataSource;
import in.sahildave.gazetti.bookmarks.sqlite.BookmarkModel;
import in.sahildave.gazetti.util.ShareButtonListener;

public class BookmarkDetailFragment extends Fragment {
    private static final String TAG = BookmarkDetailFragment.class.getName();

    public static final String HEADLINE_CLICKED = "mArticleHeadline";

    private String mArticleImageURL;
    private String mArticleURL;
    private String mArticlePubDate;
    private String mArticleHeadline;
    private String mArticleBody;
    private String[] articleContent;

    private BookmarkLoadArticleCallback mCallbacks;

    private LinearLayout mScrollToReadLayout;

    private GestureDetectorCompat mDetector;
    private Animation slide_down;

    private String npNameString;
    private String catNameString;
    private Button mReadItLater;
    private boolean bookmarked = true;
    private BookmarkDataSource dataSource;
    private Button mViewInBrowser;

    static interface BookmarkLoadArticleCallback {
        void onPreExecute(View rootView);

        void setHeaderStub(View headerStub);

        void onPostExecute(String[] result, String mArticlePubDate);
    }

    public BookmarkDetailFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof BookmarkLoadArticleCallback)) {
            throw new IllegalStateException("Activity must implement the BookmarkLoadArticleCallback interface.");
        }
        mCallbacks = (BookmarkLoadArticleCallback) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataSource = new BookmarkDataSource(getActivity());
        dataSource.open();
        setRetainInstance(true);

        if(getArguments()!=null){

            if (getArguments().containsKey(HEADLINE_CLICKED)) {
                mArticleHeadline = getArguments().getString(HEADLINE_CLICKED);
                mArticleURL = BookmarkAdapter.linkMap.get(mArticleHeadline);
                mArticlePubDate = BookmarkAdapter.pubDateMap.get(mArticleHeadline);
                mArticleBody = BookmarkAdapter.articleBody.get(mArticleHeadline);
                mArticleImageURL = BookmarkAdapter.articleImage.get(mArticleHeadline);
                npNameString = BookmarkAdapter.npNameMap.get(mArticleHeadline);
                catNameString = BookmarkAdapter.catNameMap.get(mArticleHeadline);

                Log.d(TAG, "BookmarkDetailFragment - \n"
                        +"title - :."+mArticleHeadline+"\n"
                        +"url - :."+mArticleURL+"\n"
                        +"pubDate - :."+mArticlePubDate+"\n"
                        +"image - :."+mArticleImageURL+"\n"
                        +"body - :."+mArticleBody+"\n"
                        +"catName - :."+catNameString+"\n"
                        +"npName - :."+npNameString+"\n");
            }
        }

        mDetector = new GestureDetectorCompat(getActivity(), new MyGestureListener());
        slide_down = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "BookmarkDetailFragment onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_website_detail, container, false);

        ImageButton mNewspaperTile = (ImageButton) rootView.findViewById(R.id.newspaperTile);
        mReadItLater = (Button) rootView.findViewById(R.id.read_it_later);
        mReadItLater.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_bookmark_done, 0, 0, 0);
        Button mShareButton = (Button) rootView.findViewById(R.id.shareContent);
        mViewInBrowser = (Button) rootView.findViewById(R.id.viewInBrowser);

        TextView categoryName = (TextView) rootView.findViewById(R.id.category);
        categoryName.setText(catNameString);
        categoryName.setVisibility(View.VISIBLE);

        if (npNameString.equals("The Hindu")) {
            mNewspaperTile.setImageResource(R.drawable.ic_hindu);
        } else if (npNameString.equals("The Times of India")) {
            mNewspaperTile.setImageResource(R.drawable.ic_toi);
        } else if (npNameString.equals("The Indian Express")) {
            mNewspaperTile.setImageResource(R.drawable.ic_tie);
        } else if(npNameString.equals("First Post")) {
            mNewspaperTile.setImageResource(R.drawable.ic_fp);
        }

        mNewspaperTile.setOnClickListener(webViewCalled);
        mViewInBrowser.setOnClickListener(webViewCalled);
        mReadItLater.setOnClickListener(readItLater);
        mShareButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                new ShareButtonListener(getActivity(), mArticleURL, mArticleHeadline);
            }
        });

        mScrollToReadLayout = (LinearLayout) rootView.findViewById(R.id.scrollToReadLayout);
        mScrollToReadLayout.setVisibility(View.INVISIBLE);

        ScrollView mScrollView = (ScrollView) rootView.findViewById(R.id.scroller);

        mScrollView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.d(TAG, "ScrollView ACTION_UP");
                    if (mScrollToReadLayout.getVisibility() == View.VISIBLE) {
                        mScrollToReadLayout.startAnimation(slide_down);
                        mScrollToReadLayout.setVisibility(View.INVISIBLE);
                    }
                    return mDetector.onTouchEvent(event);
                }

                return false;
            }
        });
        setHasOptionsMenu(true);

        new ArticleLoadAsyncTask(rootView).execute();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.detail_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_share:
                new ShareButtonListener(getActivity(), mArticleURL, mArticleHeadline);
                return true;
            case R.id.action_view_in_browser:
                mViewInBrowser.performClick();
                return true;
        }
        return false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    private OnClickListener webViewCalled = new OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mArticleURL)));

        }
    };

    private OnClickListener readItLater = new OnClickListener() {

        @Override
        public void onClick(View v) {
            dataSource.open();
            if (bookmarked) {
                dataSource.deleteBookmarkModelEntry(mArticleURL);
                mReadItLater.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_bookmark, 0, 0, 0);
                bookmarked=false;
            } else if (!bookmarked) {
                dataSource.createBookmarkModelEntry(getBookmarkModelObject());
                mReadItLater.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_bookmark_done, 0, 0, 0);
                bookmarked=true;
            }
        }
    };

    private BookmarkModel getBookmarkModelObject() {
        final BookmarkModel bookmarkModel = new BookmarkModel();
        try {
            bookmarkModel.setmArticleHeadline(mArticleHeadline);
            bookmarkModel.setCategoryName(catNameString);
            bookmarkModel.setNewspaperName(npNameString);
            bookmarkModel.setmArticleURL(mArticleURL);
            bookmarkModel.setmArticleBody(mArticleBody);
            bookmarkModel.setmArticleImageURL(mArticleImageURL);
            bookmarkModel.setmArticlePubDate(mArticlePubDate);
        } catch (Exception e) {
            Log.e(TAG, "Exception while creating bookmark object - " + e.getMessage(), e);
            Crashlytics.log(Log.ERROR, TAG, "Exception while creating bookmark object - " + e.getMessage());
        }
        return bookmarkModel;
    }

    public class ArticleLoadAsyncTask extends AsyncTask<Void, String, String[]> {

        View rootView;

        public ArticleLoadAsyncTask(View rootView) {
            articleContent = new String[3];
            this.rootView = rootView;
        }

        @Override
        protected void onPreExecute() {
            if (mCallbacks != null) {
                mCallbacks.onPreExecute(rootView);
            }

        }

        @Override
        protected String[] doInBackground(Void... params) {

            try {
                articleContent[0] = mArticleHeadline;
                articleContent[1] = mArticleImageURL;
                articleContent[2] = mArticleBody;
            } catch (Exception e) {
                Log.e(TAG, "Exception while reading bookmarks - " + e.getMessage(), e);
                Crashlytics.log(Log.ERROR, TAG, "Exception while reading bookmarks - " + e.getMessage());
            }

            return articleContent;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (mCallbacks != null) {
                View headerStub;
                if (mArticleImageURL == null || mArticleImageURL.equals("")) {
                    headerStub = ((ViewStub) rootView.findViewById(R.id.article_title_stub_import)).inflate();
                } else {
                    headerStub = ((ViewStub) rootView.findViewById(R.id.article_header_stub_import)).inflate();
                }
                mCallbacks.setHeaderStub(headerStub);
                mCallbacks.onPostExecute(result, mArticlePubDate);
            }
        }
    }

    @Override
    public void onResume() {
        dataSource.open();
        super.onResume();
    }

    @Override
    public void onPause() {
        dataSource.close();
        super.onPause();
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            return super.onFling(event1, event2, velocityX, velocityY);
        }
    }
}