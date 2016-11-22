package com.example.jianpan.mydoubletriangle;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private DoubleTriangleLayout doubleTriangleLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        doubleTriangleLayout = (DoubleTriangleLayout) findViewById(R.id.double_triangle_layout);
        doubleTriangleLayout.setTriangleOnClickListener(new DoubleTriangleLayout.TriangleOnClickListener() {
            @Override
            public void leftTriangleOnClick(View view) {
                Toast.makeText(MainActivity.this, "leftTriangleOnClick", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void rightTriangleOnClick(View view) {
                Toast.makeText(MainActivity.this, "rightTriangleOnClick", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
