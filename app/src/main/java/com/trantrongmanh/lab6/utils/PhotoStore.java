package com.trantrongmanh.lab6.utils;

import com.trantrongmanh.lab6.model.Photo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * In-memory singleton store for photos. Pre-populated with sample data
 * using Picsum Photos (https://picsum.photos) so the gallery looks realistic.
 *
 * In a production app this would be backed by Firebase Firestore or Storage.
 */
public class PhotoStore {

    private static PhotoStore instance;
    private final List<Photo> photos;
    private int nextId = 100;

    private PhotoStore() {
        photos = new ArrayList<>();
        seedSampleData();
    }

    public static synchronized PhotoStore getInstance() {
        if (instance == null) {
            instance = new PhotoStore();
        }
        return instance;
    }

    /**
     * Pre-populate the gallery with sample photos for demonstration.
     */
    private void seedSampleData() {
        long baseTime = System.currentTimeMillis();
        String[][] samples = {
                {"Bình Minh Trên Núi", "Bình minh tuyệt đẹp trên những dãy núi sương mù", "Thiên nhiên"},
                {"Sóng Biển", "Những con sóng xanh trong vắt vỗ vào bãi cát trắng", "Thiên nhiên"},
                {"Thành Phố Về Đêm", "Đường chân trời thành phố hiện đại lúc hoàng hôn", "Đô thị"},
                {"Con Đường Rừng", "Con đường yên tĩnh xuyên qua khu rừng xanh mướt", "Thiên nhiên"},
                {"Đồi Cát Vàng", "Những đồi cát vàng óng trải dài đến chân trời", "Thiên nhiên"},
                {"Phố Cổ", "Con phố lát đá cuội quyến rũ trong khu phố cổ", "Đô thị"},
                {"Lá Mùa Thu", "Tán lá mùa thu rực rỡ trong công viên", "Thiên nhiên"},
                {"Chợ Đêm", "Khu chợ đêm nhộn nhịp với những chiếc đèn lồng đầy màu sắc", "Đô thị"},
                {"Đỉnh Tuyết", "Đỉnh núi phủ tuyết trắng dưới bầu trời xanh trong", "Thiên nhiên"},
                {"Vườn Hoa", "Những bông hoa xuân đua nhau khoe sắc", "Thiên nhiên"},
                {"Bờ Sông", "Dòng sông êm ả chảy qua vùng nông thôn", "Thiên nhiên"},
                {"Ánh Đèn Đô Thị", "Ánh đèn neon phản chiếu trên mặt đường ướt", "Đô thị"},
                {"Đường Làng", "Con đường quê uốn lượn qua những cánh đồng xanh", "Thiên nhiên"},
                {"Cầu Hoàng Hôn", "Bóng cây cầu in lên nền trời hoàng hôn rực rỡ", "Đô thị"},
                {"Hoa Anh Đào", "Những cánh hoa anh đào hồng rụng dọc lối đi", "Thiên nhiên"},
                {"Hồ Trên Núi", "Hồ nước alpine tĩnh lặng phản chiếu những đỉnh núi", "Thiên nhiên"},
        };
        for (int i = 0; i < samples.length; i++) {
            String url = "https://picsum.photos/seed/photo" + i + "/800/600";
            photos.add(new Photo(
                    String.valueOf(i),
                    samples[i][0],
                    samples[i][1],
                    url,
                    samples[i][2],
                    baseTime - (long) (samples.length - i) * 86_400_000L,
                    i % 5 == 0  // every 5th photo is a favorite
            ));
        }
        nextId = samples.length;
    }

    public List<Photo> getAllPhotos() {
        return new ArrayList<>(photos);
    }

    public List<Photo> getFavoritePhotos() {
        List<Photo> favs = new ArrayList<>();
        for (Photo p : photos) {
            if (p.isFavorite()) favs.add(p);
        }
        return favs;
    }

    public List<Photo> getPhotosByCategory(String category) {
        List<Photo> filtered = new ArrayList<>();
        for (Photo p : photos) {
            if (p.getCategory().equals(category)) filtered.add(p);
        }
        return filtered;
    }

    public List<Photo> searchPhotos(String query) {
        List<Photo> results = new ArrayList<>();
        String lower = query.toLowerCase();
        for (Photo p : photos) {
            if (p.getTitle().toLowerCase().contains(lower) ||
                    p.getDescription().toLowerCase().contains(lower) ||
                    p.getCategory().toLowerCase().contains(lower)) {
                results.add(p);
            }
        }
        return results;
    }

    public void addPhoto(Photo photo) {
        photo.setId(String.valueOf(nextId++));
        photo.setDateAdded(System.currentTimeMillis());
        photos.add(0, photo);
    }

    public void deletePhoto(String id) {
        photos.removeIf(p -> p.getId().equals(id));
    }

    public void toggleFavorite(String id) {
        for (Photo p : photos) {
            if (p.getId().equals(id)) {
                p.setFavorite(!p.isFavorite());
                break;
            }
        }
    }

    public void setFavorite(String id, boolean favorite) {
        for (Photo p : photos) {
            if (p.getId().equals(id)) {
                p.setFavorite(favorite);
                break;
            }
        }
    }

    public int getTotalCount() {
        return photos.size();
    }

    public int getFavoriteCount() {
        int count = 0;
        for (Photo p : photos) {
            if (p.isFavorite()) count++;
        }
        return count;
    }

    /**
     * Sort photos by the given criteria.
     * @param by "date", "name", or "favorite"
     * @param ascending true for ascending order
     */
    public List<Photo> getSortedPhotos(String by, boolean ascending) {
        List<Photo> sorted = new ArrayList<>(photos);
        Comparator<Photo> comparator;
        switch (by) {
            case "name":
                comparator = Comparator.comparing(Photo::getTitle, String.CASE_INSENSITIVE_ORDER);
                break;
            case "favorite":
                comparator = (a, b) -> {
                    // Favorites first, then by date
                    int favCompare = Boolean.compare(b.isFavorite(), a.isFavorite());
                    if (favCompare != 0) return favCompare;
                    return Long.compare(b.getDateAdded(), a.getDateAdded());
                };
                break;
            case "date":
            default:
                comparator = Comparator.comparing(Photo::getDateAdded);
                break;
        }
        if (!ascending) {
            comparator = comparator.reversed();
        }
        Collections.sort(sorted, comparator);
        return sorted;
    }

    public List<Photo> getPhotos(List<Photo> list, String by, boolean ascending) {
        Comparator<Photo> comparator;
        switch (by) {
            case "name":
                comparator = Comparator.comparing(Photo::getTitle, String.CASE_INSENSITIVE_ORDER);
                break;
            case "favorite":
                comparator = (a, b) -> {
                    int favCompare = Boolean.compare(b.isFavorite(), a.isFavorite());
                    if (favCompare != 0) return favCompare;
                    return Long.compare(b.getDateAdded(), a.getDateAdded());
                };
                break;
            case "date":
            default:
                comparator = Comparator.comparing(Photo::getDateAdded);
                break;
        }
        if (!ascending) {
            comparator = comparator.reversed();
        }
        List<Photo> sorted = new ArrayList<>(list);
        Collections.sort(sorted, comparator);
        return sorted;
    }
}
