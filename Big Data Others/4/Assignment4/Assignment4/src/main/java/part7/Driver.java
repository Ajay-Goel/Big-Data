package part7;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SortedMapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class Driver {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        if (args.length != 2) {
            System.err.println("Usage: MaxSubmittedCharge <input path> <output path>");
            System.exit(-1);
        }

        Path inputPath = new Path(args[0]);
        Path outputDir = new Path(args[1]);

        Configuration conf = new Configuration(true);
        Job job = Job.getInstance(conf);

        job.setJarByClass(MyMapper.class);

        job.setMapperClass(MyMapper.class);
        job.setCombinerClass(MyCombiner.class);
        job.setReducerClass(MyReducer.class);
        job.setNumReduceTasks(1);


        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(SortedMapWritable.class);
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(MedianSDWritable.class);
        job.setNumReduceTasks(1);


        TextInputFormat.addInputPath(job, inputPath);
        FileOutputFormat.setOutputPath(job, outputDir);

        // Delete output if exists
        FileSystem hdfs = FileSystem.get(conf);
        if (hdfs.exists(outputDir)) {
            hdfs.delete(outputDir, true);
        }
        int code = job.waitForCompletion(true) ? 0 : 1;
        System.exit(code);
    }
}
