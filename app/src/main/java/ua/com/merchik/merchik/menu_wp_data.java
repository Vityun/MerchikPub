package ua.com.merchik.merchik;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

import ua.com.merchik.merchik.data.Data;


public class menu_wp_data extends toolbar_menus {
    // Declare Variables
    ListView list;
    ListViewAdapter adapter;
    EditText editsearch;
    ArrayList<Data> arraylist = new ArrayList<Data>();
    //-------------------CLASS----------------------------
    //----------------------------------------------------


    //----------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_wp_data);
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
        getSupportActionBar().setDisplayShowTitleEnabled(false);



        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); //Убирает фокус с полей ввода

        getVisualizeWPDataFromDB(); // View WP_DATA

    }//--------------------------------------------------------------------- /ON CREATE ---------------------------------------------------------------------


    /**Отображает план работ*/
    private void getVisualizeWPDataFromDB(){

        WorkPlan workPlan = new WorkPlan();


    }


    private LinearLayout getOptionsLinearLayout(long otchetId){

        LinearLayout ll = new LinearLayout(this);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(1, 1, 1, 1);

        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setId((int) otchetId);
        ll.setScrollContainer(true);

        return ll;
    }



}


