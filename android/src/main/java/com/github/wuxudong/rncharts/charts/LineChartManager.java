package com.github.wuxudong.rncharts.charts;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.ThemedReactContext;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.wuxudong.rncharts.data.DataExtract;
import com.github.wuxudong.rncharts.data.LineDataExtract;
import com.github.wuxudong.rncharts.listener.RNOnChartValueSelectedListener;
import com.github.wuxudong.rncharts.listener.RNOnChartGestureListener;
import com.github.wuxudong.rncharts.utils.BridgeUtils;

import java.util.Map;

import javax.annotation.Nullable;

public class LineChartManager extends BarLineChartBaseManager<LineChart, Entry> {

    private float axisOffset = 0;

    @Override
    public String getName() {
        return "RNLineChart";
    }

    @Override
    protected LineChart createViewInstance(ThemedReactContext reactContext) {
        LineChart lineChart =  new LineChart(reactContext);
        lineChart.setOnChartValueSelectedListener(new RNOnChartValueSelectedListener(lineChart));
        lineChart.setOnChartGestureListener(new RNOnChartGestureListener(lineChart));
        return lineChart;
    }

    @Override
    DataExtract getDataExtract() {
        return new LineDataExtract();
    }


    @Nullable
    @Override
    public Map<String, Integer> getCommandsMap() {
        Map<String, Integer> commandsMap = super.getCommandsMap();

        Map<String, Integer> map = MapBuilder.of();
        map.put("addDataPoints", ADD_DATA_POINTS);
        map.put("updateConfig", UPDATE_CONFIG);

        if (commandsMap != null) {
            map.putAll(commandsMap);
        }
        return map;
    }

    @Override
    public void receiveCommand(LineChart root, int commandId, @Nullable ReadableArray args) {
        switch (commandId) {
            case ADD_DATA_POINTS:
                addDataPoints(root, args.getMap(0));
                return;
            
            case UPDATE_CONFIG:
                updateConfig(root, args.getArray(0));
                return;
        }

        super.receiveCommand(root, commandId, args);
    }

    /**
     * Mantiene una distanza di y dal punto più alto nel dataset
     */
    @ReactProp(name = "axisOffset")
    public void setAxisOffset(LineChart chart, ReadableMap propMap) {
        if (BridgeUtils.validate(propMap, ReadableType.Number, "y")) {
            this.axisOffset = (float) propMap.getDouble("y");
        }
    }

    /**
     * Permette di aggiungere nuovi dati al grafico. Bisogna fornire un nuovo valore per tutte
     * le linee e un singolo valore di ascissa che sarà associato a tutti i nuovi punti.
     */
    private void addDataPoints(LineChart root, ReadableMap data) {
        ReadableArray dataSets = data.getArray("data");

        float maxYPoint = root.getAxisLeft().getAxisMaximum();

        for (int i = 0; i < dataSets.size(); i++) {
            ReadableMap point = dataSets.getMap(i);
            float x = (float) point.getDouble("x");
            float y = (float) point.getDouble("y");
            LineDataSet lineData = (LineDataSet) root.getData().getDataSetByIndex(i);
            lineData.addEntry(new Entry(x, y));
            if (y > maxYPoint - this.axisOffset) {
                maxYPoint = y + this.axisOffset;
            }
        }
        
        root.getAxisLeft().setAxisMaximum(maxYPoint);
        root.notifyDataSetChanged();
        root.invalidate();
    }

    /**
     * Permette di nascondere e di mettere in evidenza determinate linee.
     * Non è necessario fornire tutte le linee, basta un array con
     * oggetti che hanno l'id della linea da modificare e le proprietà da modificare.
     */
    private void updateConfig(LineChart root, ReadableArray configs) {
        
        for (int i = 0; i < configs.size(); i++) {
            ReadableMap data = configs.getMap(i);

            int id = data.getInt("id");
            ReadableMap config = data.getMap("config");

            LineDataSet lineData = (LineDataSet) root.getData().getDataSetByIndex(id);
            
            if (config.hasKey("visible")) {
                boolean visible = config.getBoolean("visible");
                lineData.setVisible(visible);
            }
            if (config.hasKey("selected")) {
                boolean selected = config.getBoolean("selected");
                lineData.setLineWidth(selected ? 3 : 1);
            }
        }

        root.notifyDataSetChanged();
        root.invalidate();
    }

}
