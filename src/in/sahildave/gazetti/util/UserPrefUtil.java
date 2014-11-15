package in.sahildave.gazetti.util;

import android.util.Log;
import in.sahildave.gazetti.homescreen.adapter.CellModel;
import in.sahildave.gazetti.util.GazettiEnums.Category;
import in.sahildave.gazetti.util.GazettiEnums.Newspapers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by sahil on 9/11/14.
 */
public class UserPrefUtil {
    private static final String LOG_TAG = UserPrefUtil.class.getName();
    private static boolean UserPrefChanged = false;

    public static List<CellModel> getUserPrefCellList(){

        List<CellModel> returnList = new ArrayList<CellModel>();
        Map<String, List<String>> userPrefMap = NewsCatFileUtil.userSelectionMap;

        GazettiEnums gazettiEnums = new GazettiEnums();
        Iterator<String> iterator = userPrefMap.keySet().iterator();
        while (iterator.hasNext()){
            String newspaper = iterator.next();
            List<String> categoriesSelected = userPrefMap.get(newspaper);
            Newspapers npEnum = gazettiEnums.getNewspaperFromName(newspaper);
            for(String category : categoriesSelected){
                Category catEnum = gazettiEnums.getCategoryFromName(category);
                CellModel cellModel = new CellModel(npEnum, catEnum);
                returnList.add(cellModel);
            }
        }

        Log.d(LOG_TAG, "Returning CellList - "+returnList);
        return returnList;
    }

    public static void replaceUserPref(CellModel oldCell, CellModel newCell){
        Map<String, List<String>> userPrefMap = NewsCatFileUtil.userSelectionMap;
        String oldNewspaper = oldCell.getNewspaperTitle();
        String oldCategory = oldCell.getCategoryTitle();
        String newNewspaper = newCell.getNewspaperTitle();
        String newCategory = newCell.getCategoryTitle();

        if(oldNewspaper.equalsIgnoreCase(newNewspaper)){
            if(userPrefMap.containsKey(oldNewspaper)) {
                List<String> categories = userPrefMap.get(oldNewspaper);
                if(categories.contains(oldCategory)){
                    categories.remove(oldCategory);
                    categories.add(newCategory);

                    userPrefMap.remove(oldNewspaper);
                    Log.d(LOG_TAG, "Removed - "+oldCell.toString()+", Added - "+newCell.toString());
                    updateUserSelectionMap(userPrefMap, oldNewspaper, categories);
                }
            }
        } else {
            deleteUserPref(oldCell);
            addUserPref(newCell);
        }
    }

    public static void addUserPref(CellModel newCell){
        Map<String, List<String>> userPrefMap = NewsCatFileUtil.userSelectionMap;
        String newspaper = newCell.getNewspaperTitle();
        String category = newCell.getCategoryTitle();

        List<String> categories;
        if(userPrefMap.containsKey(newspaper)){
            categories = userPrefMap.get(newspaper);
            if(!categories.contains(category)){
                categories.add(category);
                userPrefMap.remove(newspaper);
            }
        } else {
            categories = new ArrayList<String>();
            categories.add(category);
        }
        Log.d(LOG_TAG, "Added - "+newCell.toString());
        updateUserSelectionMap(userPrefMap, newspaper, categories);
    }

    public static void deleteUserPref(CellModel deleteCell){
        Map<String, List<String>> userPrefMap = NewsCatFileUtil.userSelectionMap;
        String newspaper = deleteCell.getNewspaperTitle();
        String category = deleteCell.getCategoryTitle();

        if(userPrefMap.containsKey(newspaper)){
            List<String> categories = userPrefMap.get(newspaper);
            if(categories.contains(category)){
                categories.remove(category);
                userPrefMap.remove(newspaper);

                Log.d(LOG_TAG, "Deleted - "+deleteCell.toString());
                updateUserSelectionMap(userPrefMap, newspaper, categories);
            }
        }
    }

    public static boolean isUserPrefChanged() {
        return UserPrefChanged;
    }

    public static void setUserPrefChanged(boolean userPrefChanged) {
        Log.d(LOG_TAG, "Setting UserPrefChanged to "+userPrefChanged);
        UserPrefChanged = userPrefChanged;
    }

    private static void updateUserSelectionMap(Map<String, List<String>> userPrefMap, String newspaper, List<String> categories) {
        userPrefMap.put(newspaper, categories);
        NewsCatFileUtil.userSelectionMap = userPrefMap;

        updateJsonMapFile();
    }

    private static void updateJsonMapFile(){
        NewsCatFileUtil.getInstance().convertUserFeedMapToJsonMap();
    }

}
