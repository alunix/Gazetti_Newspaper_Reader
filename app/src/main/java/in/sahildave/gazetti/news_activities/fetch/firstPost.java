package in.sahildave.gazetti.news_activities.fetch;

import com.crashlytics.android.Crashlytics;
import in.sahildave.gazetti.util.ConfigService;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class firstPost {
    final String LOG_TAG = this.getClass().getName();

    String mArticleURL;
    String titleText;
    String mImageURL = null;
    String articleText = "";

    public firstPost(String mArticleURL) {
        this.mArticleURL = mArticleURL;
    }

    public String[] getFirstPostArticleContent() {

        Document doc;
        String[] result = new String[3];
        String url = mArticleURL;

        try {
            Connection connection = Jsoup.connect(url).userAgent("Mozilla").timeout(10 * 1000);
            Response response = connection.execute();

            if(response==null){
                Crashlytics.log("Is response null ? "+(null==response));
                return null;
            } else if(response.statusCode() !=200){
                Crashlytics.log("Received response - "+response.statusCode()+" -- "+response.statusMessage());
                Crashlytics.log("Received response - "+response.body());
                return null;
            }

            doc = connection.get();

            Element bodyElement = doc.body();

            // get Title
            String FirstPostTitleXPath = ConfigService.getInstance().getFirstPostHead();
            Elements titleElements = bodyElement.select(FirstPostTitleXPath);
            titleText = titleElements.first().text();

            // get HeaderImageUrl
            mImageURL = getImageURL(bodyElement);

            String FirstPostArticleXPath = ConfigService.getInstance().getFirstPostBody();
            Elements articleElements = doc.select(FirstPostArticleXPath);

            for(Element el: articleElements){
                String temp = el.text();
                if(temp.length()>0){
                    articleText = articleText + temp + "\n\n";
                }
            }

            result[0] = titleText;
            result[1] = mImageURL;
            result[2] = articleText;

        } catch (IOException e) {
            Crashlytics.logException(e);
            return null;
        } catch (NullPointerException npe) {
            articleText = null;
            Crashlytics.logException(npe);
            return null;
        } catch (Exception e) {
            Crashlytics.logException(e);
            return null;
        }

        return result;

    }

    private String getImageURL(Element bodyElement) {
        Elements mainImageElement = bodyElement.select(ConfigService.getInstance().getFirstPostImage());

        if (mainImageElement.size() != 0) {
            mImageURL = mainImageElement.get(1).attr("src");
        }

        return mImageURL;

    }
}
