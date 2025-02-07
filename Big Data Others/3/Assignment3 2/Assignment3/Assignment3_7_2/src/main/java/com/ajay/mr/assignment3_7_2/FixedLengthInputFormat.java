/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ajay.mr.assignment3_7_2;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

/**
 * FixedLengthInputFormat is an input format which can be used
 * for input files which contain fixed length records with NO
 * delimiters and NO carriage returns (CR, LF, CRLF) etc. Such
 * files typically only have one gigantic line and each "record"
  actual
 * value is shorter than the fixed length.<BR><BR>
 * 
 * Users must configure the record length property before submitting
 * any jobs which use FixedLengthInputFormat.<BR><BR>
 * 
 * FixedLengthInputFormat.setRecordLength(myJob,[myFixedRecordLength]);<BR><BR>
 * 
 * Secondly users can optionally configure the record key start/end properties 
 * before submitting any jobs which use FixedLengthInputFormat. 
 * These properties define the byte position to start at and subsequently end at 
 * for the record key. There are multiple options for setting the property
 * programatically. If NOT set, the key returned will be the record position
 * within the InputSplit that the record was read from. <BR><BR>
 * 
 * FixedLengthInputFormat.setRecordKeyStartAt(myJob,[start at position, INCLUSIVE]);<BR>
 * FixedLengthInputFormat.setRecordKeyEndAt(myJob,[end at position, INCLUSIVE]);<BR>
 * FixedLengthInputFormat.setRecordKeyBoundaries(myJob,[start],[end]);<BR><BR>
 * 
 * This input format overrides <code>computeSplitSize()</code> in order to ensure
 * that InputSplits do not contain any partial records since with fixed records
 * there is no way to determine where a record begins if that were to occur.
 * Each InputSplit passed to the FixedLengthRecordReader will start at the 
 * beginning of a record, and the last byte in the InputSplit will be the last 
 * byte of a record. The override of <code>computeSplitSize()</code> delegates 
 * to FileInputFormat's compute method, and then adjusts the returned split size 
 * by doing the following:
 * <code>(fileInputFormatsComputedSplitSize / fixedRecordLength) * fixedRecordLength</code>
 *
 * <BR><BR>
 * This InputFormat returns a FixedLengthRecordReader. <BR><BR>
 * 
 * Compressed files currently are not supported.
 *
 * @see    FixedLengthRecordReader
 *
 */
public class FixedLengthInputFormat 
    extends FileInputFormat<BytesWritable, BytesWritable> {


  /**
   * When using FixedLengthInputFormat you MUST set this
   * property in your job configuration to specify the fixed
   * record length.
   * <BR><BR>
   * 
   * i.e. 
   
   *     [myFixedRecordLength]);
   * <BR><BR>
   * OR<BR><BR>
   * FixedLengthInputFormat.setRecordLength(myJob,[myFixedRecordLength]);
   * 
   */
  public static final String FIXED_RECORD_LENGTH = 
    "mapreduce.input.fixedlengthinputformat.record.length"; 
  
  
  public static final String FIXED_RECORD_KEY_START_AT = 
    "mapreduce.input.fixedlengthinputformat.recordkey.startat";

 
  /**
   * When using FixedLengthInputFormat you can set this
   * property in your job configuration to specify the byte position
   * within each record where they KEY value returned by FixedLengthRecordReader
   
   * <BR><BR>
   * OR<BR><BR>
   * FixedLengthInputFormat.setRecordKeyEndAt(myJob,[endAtPosition]);
   * <BR><BR>
   * OR<BR><BR>
   * FixedLengthInputFormat.setRecordKeyBoundaries(myJob,[startAtPosition],
   *                                                     [endAtPosition]);
   * 
   */
  public static final String FIXED_RECORD_KEY_END_AT = 
    "mapreduce.input.fixedlengthinputformat.recordkey.endat";
  
  // our logger reference
  private static final Log LOG = 
    LogFactory.getLog(FixedLengthInputFormat.class);

  // the default fixed record length (-1), error if this does not change
  private int recordLength = -1;

  // the start position for each records KEY
  // default -1 means not set, not used by record reader
  private int recordKeyStartAt = -1;
  
  // the end position for each records KEY
  // default -1 means not set, not used by record reader
  private int recordKeyEndAt = -1;
  
  /**
   * Set the length of each record
   * @param job the job to modify
   * @param recordLength the length of a record
   */
  public static void setRecordLength(Job job, int recordLength) {
    job.getConfiguration().setInt(FIXED_RECORD_LENGTH, recordLength);
  }
  
  /**
   * Set the ending position of a fixed record's key value
   * @param job the job to modify
   * @param endAt the end position within a fixed record, that defines the last
   *        byte for the record's key value (Zero based)
   */
  public static void setRecordKeyEndAt(Job job, int endAt) {
    job.getConfiguration().setInt(FIXED_RECORD_KEY_END_AT, endAt);
  }
  
  /**
   * Set the starting position of a fixed record's key value
   * @param job the job to modify
   * @param startAt the start position within a fixed record, that defines the first
   *        byte for the record's key value (Zero based)
   */
  public static void setRecordKeyStartAt(Job job, int startAt) {
    job.getConfiguration().setInt(FIXED_RECORD_KEY_START_AT, startAt);
  }
  
  /**
   * Get the ending position of a fixed record's key value
   * @param conf the configuration to fetch the property from
   * return the end position within a fixed record, that defines the last
   *        byte for the record's key value (Zero based, INCLUSIVE)
   */
  public static int getRecordKeyEndAt(Configuration conf) {
    return conf.getInt(FIXED_RECORD_KEY_END_AT, -1);
  }
  
  
  /**
   * Get the starting position of a fixed record's key value
   * @param conf the configuration to fetch the property from
   * return the start position within a fixed record, that defines the first
   *        byte for the record's key value (Zero based, INCLUSIVE)
   */
  public static int getRecordKeyStartAt(Configuration conf) {
    return conf.getInt(FIXED_RECORD_KEY_START_AT, -1);
  }
  
  /**
   * Get record length value
   * @param conf  the Configuration
   * return the record length, zero means none was set
   */
  public static int getRecordLength(Configuration conf) {
    return conf.getInt(FIXED_RECORD_LENGTH, 0);
  }

  
  /**
   * Set the starting and ending position of a fixed record's key value
   * 
   * @param job the job to modify
   * @param startAt the start position within a fixed record, that defines the first
   *        byte for the record's key value (Zero based, INCLUSIVE)
   * @param endAt the end position within a fixed record, that defines the last
   *        byte for the record's key value (Zero based, INCLUSIVE)
   */
  public static void setRecordKeyBoundaries(Job job, int startAt, int endAt) {
    setRecordKeyStartAt(job,startAt);
    setRecordKeyEndAt(job,startAt);
  }

  /**
   * Return the int value from the given Configuration found
   * by the FIXED_RECORD_LENGTH property.
   * 
   * @param config
   * @return    int record length value
   * @throws IOException if the record length found is 0 (non-existant, 
   *     not set etc)
   */
  private static int getAndValidateRecordLength(Configuration config) throws IOException {
    int recordLength = 
      config.getInt(FixedLengthInputFormat.FIXED_RECORD_LENGTH, 0); 

    // this would be an error
    if (recordLength == 0) {
      throw new IOException("FixedLengthInputFormat requires the Configuration"+
              " property:" + FIXED_RECORD_LENGTH + " to" +
              " be set to something > 0. Currently the value is 0 (zero)");
    }

    return recordLength;
  }

  /**
   * This input format overrides <code>computeSplitSize()</code> in order to ensure
   * that InputSplits do not contain any partial records since with fixed records
   * there is no way to determine where a record begins if that were to occur.
   * Each InputSplit passed to the FixedLengthRecordReader will start at the 
   * beginning of a record, and the last byte in the InputSplit will be the last 
   * byte of a record.The override of <code>computeSplitSize()</code> delegates 
   * to FileInputFormat's compute method, and then adjusts the returned split 
   * size by doing the following:
   * <code>(fileInputFormatsComputedSplitSize / fixedRecordLength) * fixedRecordLength</code>
   * 
   * @inheritDoc
   */
  @Override
  protected long computeSplitSize(long blockSize, long minSize, long maxSize) {
    long defaultSize = 
      super.computeSplitSize(blockSize, minSize, maxSize);

    // 1st, if the default size is less than the length of a
    // raw record, lets bump it up to a minimum of at least ONE record length
    if (defaultSize <= recordLength) {
      return recordLength;
    }

    // determine the split size, it should be as close as possible to the 
    // default size, but should NOT split within a record... each split
    // should contain a complete set of records with the first record
    // starting at the first byte in the split and the last record ending
    // with the last byte in the split.
    long splitSize =  (defaultSize / recordLength) * recordLength;
    
    LOG.info("FixedLengthInputFormat: calculated split size: " + splitSize);

    return splitSize;

  }

  /**
   * Returns a FixedLengthRecordReader instance
   * 
   * @inheritDoc
   */
  @Override
  public RecordReader<BytesWritable, BytesWritable> createRecordReader(InputSplit split,
      TaskAttemptContext context) throws IOException, InterruptedException {
    return new FixedLengthRecordReader();
  }

  /**
   * Validates that a valid FIXED_RECORD_LENGTH config property
   * has been set and if so, returns the splits. If the FIXED_RECORD_LENGTH
   * property has not been set, this will throw an IOException.
   * 
   * @inheritDoc
   */
  @Override
  public List<InputSplit> getSplits(JobContext job) throws IOException {
	  
    // fetch configuration
	Configuration conf = job.getConfiguration();

	// ensure recordLength is properly setup
    try {
      if (this.recordLength == -1) {
        this.recordLength = getAndValidateRecordLength(job.getConfiguration());
      }
      LOG.info("FixedLengthInputFormat: my fixed record length is: " + 
              recordLength);

    } catch(Exception e) {
	    throw new IOException("FixedLengthInputFormat requires the" +
              " Configuration property:" + FIXED_RECORD_LENGTH + " to" +
              " be set to something > 0. Currently the value is 0 (zero)");
    }
    
    // ensure recordKey start/end is setup properly if it was defined by the user
	if (this.recordKeyStartAt == -1) {
		
		this.recordKeyStartAt = FixedLengthInputFormat.getRecordKeyStartAt(conf); 
		this.recordKeyEndAt = FixedLengthInputFormat.getRecordKeyEndAt(conf);
		
		// if one is set, they BOTH must be set, this is an error
		// if endAt < startAt, this is an error
		// if either is > record length, this is an error
		// if either are < -1 (default), this is an error
		if ((recordKeyStartAt >= 0 && recordKeyEndAt == -1) ||
			(recordKeyStartAt == -1 && recordKeyEndAt >= 0) ||
			(recordKeyEndAt < recordKeyStartAt) ||
			(recordKeyEndAt > recordLength) ||
			(recordKeyStartAt > recordLength) ||
			(recordKeyStartAt < -1) ||
			(recordKeyEndAt < -1)) {
				
          throw new IOException("FixedLengthInputFormat requires the" +
            " optional configuration properties:" + FIXED_RECORD_KEY_START_AT + 
            " and" + FIXED_RECORD_KEY_END_AT + " to A) be less than the "+
            " fixed record length. B) both must be set together C) neither " +
            " can be less than 0. D) end at must be > start at.");
		} 	
	}

    return super.getSplits(job);
  }
  
  
  /**
   * @inheritDoc
   */
  @Override
  protected boolean isSplitable(JobContext context, Path file) {
    CompressionCodec codec = 
      new CompressionCodecFactory(context.getConfiguration()).getCodec(file);
    if (codec != null) {
      return false; 
    }

    return true; 
  } 

}