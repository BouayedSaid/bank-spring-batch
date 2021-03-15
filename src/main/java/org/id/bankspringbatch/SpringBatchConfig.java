package org.id.bankspringbatch;

import org.id.bankspringbatch.entities.BankTransaction;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.id.bankspringbatch.entities.BankTransaction;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableBatchProcessing
public class SpringBatchConfig {
	
	@Autowired
	private JobBuilderFactory jobBuilderFactory;
	@Autowired
	private StepBuilderFactory stepBuilderFactory;
	@Autowired
	private ItemReader<BankTransaction> bankTransactionItemReader;
	@Autowired
	private ItemWriter<BankTransaction> bankTransactionItemWriter;
	//@Autowired private ItemProcessor<BankTransaction, BankTransaction> bankTransactionItemProcessor;
	
	@Bean
	public Job bankJob(){
		Step step1=stepBuilderFactory.get("step-load-data")
			.<BankTransaction, BankTransaction>chunk(100)
			.reader(bankTransactionItemReader)
		//	.processor(bankTransactionItemProcessor)
			.processor(compositeItemProcessor())
			.writer(bankTransactionItemWriter)
			.build();
		return jobBuilderFactory.get("bank-data-loader-job")
			.start(step1).build();
	}
	
	@Bean
	public ItemProcessor<BankTransaction,BankTransaction> compositeItemProcessor() {
		List<ItemProcessor<BankTransaction, BankTransaction>> itemProcessors = new ArrayList<>();
		itemProcessors.add(itemProcessor1());
		itemProcessors.add(itemProcessor2());
		CompositeItemProcessor<BankTransaction, BankTransaction> compositeItemProcessor = new CompositeItemProcessor<>();
		compositeItemProcessor.setDelegates(itemProcessors);
		return compositeItemProcessor;
	}
	
	@Bean
	BankTransactionItemAnalyticsProcessor itemProcessor2() {
		return new BankTransactionItemAnalyticsProcessor();
	}
	
	@Bean
	BankTransactionItemProcessor itemProcessor1() {
		return new BankTransactionItemProcessor();
	}
	
	@Bean
	public FlatFileItemReader<BankTransaction> flatFileItemReader(@Value("${inputFile}") Resource inputFile){
	
		FlatFileItemReader<BankTransaction> fileItemReader = new FlatFileItemReader<>();
		fileItemReader.setName("FFIR1");
		fileItemReader.setLinesToSkip(1);
		fileItemReader.setResource(inputFile);
		fileItemReader.setLineMapper(lineMapper());
		return fileItemReader;
	}
	
	@Bean
	public LineMapper<BankTransaction> lineMapper() {
		DefaultLineMapper<BankTransaction> lineMapper = new DefaultLineMapper<>();
		DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
		lineTokenizer.setDelimiter(",");
		lineTokenizer.setStrict(false);
		lineTokenizer.setNames("id","accountID","strTransactionDate","transactionType","amount");
		lineMapper.setLineTokenizer(lineTokenizer);
		BeanWrapperFieldSetMapper fieldSetMapper = new BeanWrapperFieldSetMapper();
		fieldSetMapper.setTargetType(BankTransaction.class);
		lineMapper.setFieldSetMapper(fieldSetMapper);
		return lineMapper;
	}
	
/*	@Bean
	public ItemProcessor<BankTransaction, BankTransaction> itemProcessor(){
		return new ItemProcessor<BankTransaction, BankTransaction>() {
			@Override
			public BankTransaction process(BankTransaction bankTransaction) throws Exception {
				throw new NotImplementedException("");
			}
		}
	}*/
}
