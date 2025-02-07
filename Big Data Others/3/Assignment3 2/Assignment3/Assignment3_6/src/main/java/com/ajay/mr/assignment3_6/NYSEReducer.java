/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ajay.mr.assignment3_6;

import java.io.IOException;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 *
 * @author ajaygoel
 */
public class NYSEReducer extends Reducer<Text, FloatWritable, Text, FloatWritable>{

    @Override
    protected void reduce(Text key, Iterable<FloatWritable> values, Context context) throws IOException, InterruptedException {
        float max= 0;
        FloatWritable result = new FloatWritable();
        for(FloatWritable f:values){
            if(max<f.get())
                max=f.get();
        }
        result.set(max);
        context.write(key, result);
    }
    
}
