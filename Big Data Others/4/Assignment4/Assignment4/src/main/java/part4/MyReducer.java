package part4;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class MyReducer extends Reducer<CompositeKeyWritable, NullWritable,CompositeKeyWritable,NullWritable> {
    @Override
    protected void reduce(CompositeKeyWritable key, Iterable<NullWritable> values,
                          Reducer<CompositeKeyWritable, NullWritable, CompositeKeyWritable, NullWritable>.Context context) throws IOException, InterruptedException {
        for(NullWritable v: values){
            context.write(key,v);
        }
    }
}
