package tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated;

import tk.lefourretoutsonore.lefourre_toutsonore.R;

/**
 * Created by M4gicT0 on 14/01/2016.
 */
public enum PlayListChoice {

    ALL("ALL", "Tous les sons", "", -1),
    LIKES("LIKES", "Mes likes", "", -1),
    REGGAE("1", "Reggae", "Roots - Dub - Raga - Jungle", R.drawable.ban_reggae),
    ELECTRO("2", "Electro", "House - Techno - Minimale - Tribe", R.drawable.ban_electro),
    TRANCE("3", "Trance", "Progressive - Goa - Forest - Tribal", R.drawable.ban_trance),
    POP("5", "Pop", "Variété française - Soul - Country - Pop Rock", R.drawable.ban_pop),
    CORE("6", "Core", "Frenchcore - Hardcore - Hardtek - Acidcore", R.drawable.ban_core),
    HIPHOP("7", "Hip-Hop", "Trap - Rap - R'n'B - Trip Hop", R.drawable.ban_hiphop),
    ROCK("4", "Rock n' Roll", "Folk - Hard Rock - Blues - Jazz", R.drawable.ban_rock);

    private String id;
    private String longName;
    private String desc;
    private int banId;

    PlayListChoice(String id, String longName, String desc, int banId) {
        this.id = id;
        this.longName = longName;
        this.desc = desc;
        this.banId = banId;
    }

    public String getId() { return id; }
    public String getLongName() { return longName; }
    public String getDesc() { return desc; }
    public int getBanId() { return banId; }
}
