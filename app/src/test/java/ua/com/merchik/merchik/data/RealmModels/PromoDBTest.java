package ua.com.merchik.merchik.data.RealmModels;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.junit.Test;

import ua.com.merchik.merchik.data.RetrofitResponse.models.PromoTableResponce;
import ua.com.merchik.merchik.retrofit.FlexibleIntegerTypeAdapter;

import static org.junit.Assert.*;

public class PromoDBTest {
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(int.class, new FlexibleIntegerTypeAdapter(0))
            .create();

    @Test
    public void newPromoHasZeroDiscount() {
        assertEquals(0, new PromoDB().getDiscountPercent());
    }

    @Test
    public void discountCanBeSetAndCleared() {
        PromoDB promo = new PromoDB();
        promo.setDiscountPercent(25);
        assertEquals(25, promo.getDiscountPercent());
        promo.setDiscountPercent(0);
        assertEquals(0, promo.getDiscountPercent());
    }

    @Test
    public void readsNumericDiscount() {
        PromoDB promo = gson.fromJson("{\"ID\":\"357\",\"discount_percent\":20}", PromoDB.class);
        assertEquals("357", promo.getID());
        assertEquals(20, promo.getDiscountPercent());
    }

    @Test
    public void readsStringDiscount() {
        PromoDB promo = gson.fromJson("{\"discount_percent\":\"15\"}", PromoDB.class);
        assertEquals(15, promo.getDiscountPercent());
    }

    @Test
    public void absentNullAndEmptyDiscountDefaultToZero() {
        for (String json : new String[]{"{}", "{\"discount_percent\":null}", "{\"discount_percent\":\"\"}"}) {
            assertEquals(json, 0, gson.fromJson(json, PromoDB.class).getDiscountPercent());
        }
    }

    @Test
    public void serializesUsingServerFieldName() {
        PromoDB promo = new PromoDB();
        promo.setDiscountPercent(30);
        JsonObject json = gson.toJsonTree(promo).getAsJsonObject();
        assertEquals(30, json.get("discount_percent").getAsInt());
        assertFalse(json.has("discountPercent"));
    }

    @Test
    public void responsePreservesOtherPromoFields() {
        String json = "{\"state\":true,\"list\":[{\"ID\":\"357\",\"nm\":\"Promo\","
                + "\"isGrp\":\"0\",\"parentId\":\"1\",\"dtUpdate\":\"1792010000\","
                + "\"authorId\":\"123\",\"discount_percent\":\"10\"}]}";
        PromoTableResponce response = gson.fromJson(json, PromoTableResponce.class);
        assertTrue(response.getState());
        PromoDB promo = response.getList().get(0);
        assertEquals("357", promo.getID());
        assertEquals("Promo", promo.getNm());
        assertEquals("0", promo.getIsGrp());
        assertEquals("1", promo.getParentId());
        assertEquals("1792010000", promo.getDtUpdate());
        assertEquals("123", promo.getAuthorId());
        assertEquals(10, promo.getDiscountPercent());
    }
}
