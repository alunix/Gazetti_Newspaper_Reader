package com.example.try_masterdetail.news_activities.fetch;

import com.example.try_masterdetail.util.ConfigService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class hindu {

    String mArticleURL;
	String titleText;
	String mImageURL = null;
	String bodyText = "";
	String mArticlePubDate;

	public hindu(String mArticleURL, String mArticlePubDate) {
		this.mArticleURL = mArticleURL;
		this.mArticlePubDate = mArticlePubDate;
	}

	public String[] getHinduArticle() {

		Document doc;
		String[] result = new String[4];
		String url = mArticleURL;

		try {

			doc = Jsoup.connect(url) //
					.userAgent("Mozilla") //
					.timeout(10 * 1000) //
					.get(); //

			// get Body
			Element bodyElement = doc.body();

			// get Title
			String HinduTitleXPath = ConfigService.getTheHinduHead();
			Elements titleElements = bodyElement.select(HinduTitleXPath);
			titleText = titleElements.first().text();

			// get HeaderImageUrl
			mImageURL = getImageURL(bodyElement);

			String HinduArticleXPath = ConfigService.getTheHinduBody();
			Elements bodyArticleElements = bodyElement.select(HinduArticleXPath);
			for (Element textArticleElement : bodyArticleElements) {
				bodyText += textArticleElement.text() + "\n\n";
			}

            result[0] = titleText;
            result[1] = mImageURL;
            result[2] = bodyText;
            result[3] = mArticlePubDate;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException npe){
            npe.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

		return result;

	}

	private String getImageURL(Element bodyElement) {
        Elements mainImageElement = bodyElement.select(ConfigService.getTheHinduImageFirst());
        Elements carouselElements = bodyElement.select(ConfigService.getTheHinduImageSecond());

        if (mainImageElement.size() != 0) {
			mImageURL = mainImageElement.first().attr("src");
		} else if (carouselElements.size() != 0) {
			Elements carouselImage = carouselElements.select("div#pic").first().select("img");
			mImageURL = carouselImage.attr("src");
		}

		return mImageURL;

	}
}
