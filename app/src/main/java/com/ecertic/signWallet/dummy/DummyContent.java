package com.ecertic.signWallet.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ecertic.signWallet.R;

/**
 * Just dummy content. Nothing special.
 *
 * Created by Andreas Schrade on 14.12.2015.
 */
public class DummyContent {

    /**
     * An array of sample items.
     */
    public static List<DummyItem> ITEMS = new ArrayList<>();

    /**
     * A map of sample items. Key: sample ID; Value: Item.
     */
    public static final Map<String, DummyItem> ITEM_MAP = new HashMap<>(5);

    static {
        //addItem(new DummyItem("1", R.drawable.p1, "Contrato Galp", "Tarjeta Fast", "Te regalamos tus primeros 600 puntos si solicitas hoy tu tarjeta Fast."));
        //addItem(new DummyItem("2", R.drawable.p2, "Contrato Bein", "beIN SPORTS CONNECT 9,99€","Disfrutalo estés donde estés."));
        //addItem(new DummyItem("3", R.drawable.p3, "Contrato Santander", "Cuenta 123", "1% 2% 3% Bonificaciones."));
        //addItem(new DummyItem("4", R.drawable.p4, "Contrato Salesland", "Contrata Hoy tu Seguro","¡Te puede salir Gratis el de Moto y Hogar!"));
        //addItem(new DummyItem("5", R.drawable.p5, "Contrato Galp", "Steve Jobs","Deciding what not do do is as important as deciding what to do."));
        //addItem(new DummyItem("6", R.drawable.p5, "Contrato Galp", "Steve Jobs","Deciding what not do do is as important as deciding what to do."));

        //DummyContent.ITEMS.add(new DummyContent.DummyItem("5",R.drawable.p5,"Contrato Tal","Empresa","Etc."));
    }

    public static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public void changeSignState(String id,boolean status){
       ITEMS.get(Integer.valueOf(id)-1).isSigned = status;

    }

    public static class DummyItem {

        public static final int LISTO = 0;
        public static final int FINALIZADO = 1;
        public static final int PENDIENTE_DE_ENVIO = 2;
        public static final int ADVERTENCIA = 3;
        public static final int CADUCADO = 4;
        public static final int ERROR = 5;



        public final String id;
        public final int photoId;
        public final String title;
        public final String author;
        public final String content;
        public Boolean isSigned;
        public int status;

        public DummyItem(String id, int photoId, String title, String author, String content) {
            this.id = id;
            this.photoId = photoId;
            this.title = title;
            this.author = author;
            this.content = content;
            this.isSigned = false;
            //this.status = DummyItem.LISTO;
        }


    }
}
