package indi.jackc.relev.apriori;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Apriori {
	private final static  int SUPPORT=2;//支持度阀值
	private final static double CONFIDENCE=0.7;//置信区间阀值
	private final static String ITEM_SPLIT=","; //项之间的分隔符
	private final static String CON=" --> ";//强关联项之间的分隔符
	private final static List<String> transList=new ArrayList<String>();//所有交易
	
	//初始化交易记录
	static{
		transList.add("1,2,5,");
		transList.add("2,4,");
		transList.add("2,3,");
		transList.add("1,2,4,");
		transList.add("1,3,");
		transList.add("2,3,");
		transList.add("1,4,");
		transList.add("1,2,3,5,");
		transList.add("1,2,3,");
		transList.add("2,3,5,");
	}
	
	private Map<String, Integer> getFC() {
		// TODO Auto-generated method stub
		Map<String,Integer> frequentCollectionMap=new HashMap<String ,Integer>();//所有的频繁项集
		frequentCollectionMap.putAll(getItem1FC());
		
		Map<String ,Integer> itemKFCMap=new HashMap<String , Integer>();//频繁K项集
		
		/**
		 * 错误：改变了frequentCollectionMap在内存中的数据
		 */
		//itemKFCMap=frequentCollectionMap;
		
		itemKFCMap.putAll(getItem1FC());
		
		while(itemKFCMap!=null && itemKFCMap.size()!=0){
			//候选项集
			Map<String ,Integer> candidateCollection=getCandidateCollection(itemKFCMap);
			
			//候选项集的key的集合
			Set<String> cCKeySet = candidateCollection.keySet();
			
			//对候选项进行累加计数
			for(String trans:transList){
				for(String cCKey:cCKeySet){
					
					//判断交易中是否出现该候选项
					boolean flag=true;
					
					String items [] =cCKey.split(ITEM_SPLIT);
					
					//若有一个元素不存在，则trans中必不包含cCKey
					for(String item:items){
						if(trans.indexOf(item+ITEM_SPLIT)==-1){
							flag=false;
							break;
						}
					}
					
					if(flag){
						candidateCollection.put(cCKey, candidateCollection.get(cCKey)+1);
					}
				}
			}
			
			//根据支持度的限定值，过滤非频繁项集
			
			//清空频繁k项集
			itemKFCMap.clear();
			for(String candidate:cCKeySet){
				if(candidateCollection.get(candidate)>=SUPPORT){
					itemKFCMap.put(candidate, candidateCollection.get(candidate));
				}
			}
			
			//合并所有频繁项集
			frequentCollectionMap.putAll(itemKFCMap);
		}
		
		
		
		return frequentCollectionMap;
	}
	
	private Map<String, Integer> getCandidateCollection(Map<String, Integer> itemKFCMap) {
		//所有的候选项集
		Map<String ,Integer> candidateCollection=new HashMap<String ,Integer>();
		
		Set<String> itemSet1=itemKFCMap.keySet();
		Set<String> itemSet2=itemKFCMap.keySet();
		
		for(String item1:itemSet1){
			for(String item2:itemSet2){
				String temp1 [] = item1.split(ITEM_SPLIT);
				String temp2 [] = item2.split(ITEM_SPLIT);
				
				//可能的候选项
				String c="";
				
				if(temp1.length==1){
					//只有一个元素，不相同的元素直接连接
					if(temp1[0].compareTo(temp2[0])<0){
						c=temp1[0]+ITEM_SPLIT+temp2[0]+ITEM_SPLIT;
					}
				}else{
					boolean flag=true;
					for(int i=0;i<temp1.length-1;i++){
						//由于项集是有序的，每次取最后一个元素不同的两组进行合并，保证每个候选项是k+1个元素
						if(!temp1[i].equals(temp2[i])){
							flag=false;
							break;
						}
					}
					//为避免重复，选择最后一个元素较小的那组
					if(flag && temp1[temp1.length-1].compareTo(temp2[temp2.length-1])<0){
						c=item1+temp2[temp2.length-1]+ITEM_SPLIT;
					}
				}
				
				//根据Apriori的先验性质，即频繁项集的子集必为频繁项集，进行剪枝
				
				//记录是否存在非频繁的子集
				boolean hasInfrequentSubSet = false;
				
				if(!c.equals("")){
					String items [] = c.split(ITEM_SPLIT);
					for(int i=0;i<items.length;i++){
						//记录除去第i个元素的子项
						String subC="";
						for(int j=0;j<items.length;j++){
							if(i!=j){
								subC=subC+items[j]+ITEM_SPLIT;
							}
						}
						if(!itemKFCMap.containsKey(subC)){
							hasInfrequentSubSet=true;
							break;
						}
					}
				}else{
					hasInfrequentSubSet=true;
				}
				
				//若c（包含k+1项个子元素）的所有包含k个元素的子项均为非频繁项(不用检查k-1个元素的子项)，则该项可放入候选集
				if(!hasInfrequentSubSet){
					candidateCollection.put(c, 0);
				}
			}
		}
		
		return candidateCollection;
	}

	private Map<String, Integer> getItem1FC() {
		// TODO Auto-generated method stub
		//频繁1项集	
		Map<String ,Integer> item1FCMap=new HashMap<String, Integer>();
		
		for(String trans:transList){
			String items[]=trans.split(ITEM_SPLIT);
			for(String item:items){
				if(!item1FCMap.containsKey(item+ITEM_SPLIT)){
					item1FCMap.put(item+ITEM_SPLIT, 1);
				}else{
					item1FCMap.put(item+ITEM_SPLIT, item1FCMap.get(item+ITEM_SPLIT)+1);
				}
			}
		}
		
		/**
		 * 遍历list时做删除操作，下方法错误
		 * 原因:造成内存泄漏
		 * 使用下标list[i]进行删除时，删除一个元素后，后面的元素会自动前移，若要删除两个连续的元素，则出错
		 */
		/*for(String key:item1FCMap.keySet()){
			if(item1FCMap.get(key)<SUPPORT){
				item1FCMap.remove(key);
			}
		}*/
		
		/**
		 * 正确做法：使用迭代器
		 */
		Iterator<Map.Entry<String, Integer>> iterator =item1FCMap.entrySet().iterator();
		while(iterator.hasNext()){
			Map.Entry<String, Integer> item=iterator.next();
			if(item.getValue()<SUPPORT){
				iterator.remove();
			}
		}
		return item1FCMap;
	}
	
	private Map<String, Double> getRelationRules(Map<String, Integer> frequentCollectionMap) {
		// TODO Auto-generated method stub
		
		//记录关联规则
		Map<String,Double> relationRulesMap=new HashMap<String, Double>();
		
		Set<String> fKeyMap=frequentCollectionMap.keySet();
		
		for(String fKey:fKeyMap){
			String items [] =fKey.split(ITEM_SPLIT);
			double countAll=frequentCollectionMap.get(fKey);
			if(items.length>1){
				List<String> source=new ArrayList<String>();
				Collections.addAll(source, items);
				List<List<String>> result=new ArrayList<List<String>>();
				
				//计算当前频繁项中所有元素组成的子集
				buildSubSet(source, result);
				
				//遍历当前频繁项集的子集（result），计算相关性
				for(List<String> rList:result){
					
					//仅遍历真子集
					if(rList.size()<source.size()){
						
						//记录当前频繁项中不属于当前子项的部分
						List<String> otherList=new ArrayList<String>();
						
						for(String sItem:source){
							if(!rList.contains(sItem)){
								otherList.add(sItem);
							}
						}

						String reasonPart="";
						String otherPart="";
						
						for(String rItem:rList){
							reasonPart=reasonPart+rItem+ITEM_SPLIT;
						}
						
						for(String oItem:otherList){
							otherPart=otherPart+oItem+ITEM_SPLIT;
						}
						
						//当前子项在交易中出现的次数
						double reasonCount=frequentCollectionMap.get(reasonPart);
						
						//计算置信度的值
						double itemConfidence=countAll/reasonCount;
						
						//若结果大于阀值，则存在强关联关系：reasonPart强关联otherPart
						if(itemConfidence>=CONFIDENCE){
							String rule=reasonPart+CON+otherPart;
							relationRulesMap.put(rule, itemConfidence);
						}
					}
				}
			}
		}
		return relationRulesMap;
	}

	//递归求出source的所有子集
	private void buildSubSet(List<String> source, List<List<String>> result) {
		// TODO Auto-generated method stub
		
		//仅包含一个元素时，直接添加至result
		if(source.size()==1){
			List<String> temp=new ArrayList<String>();
			temp.add(source.get(0));
			result.add(temp);
		}else{
			//当有n（n>1）个元素时，转为求n-1个元素的子集
			buildSubSet(source.subList(0, source.size()-1), result);
			
			//获取当前result中的size，以便在接下来控制循环
			int size=result.size();
			
			//将当期的第n个元素加入到结果中
			List<String> single =new ArrayList<String>();
			single.add(source.get(source.size()-1));
			result.add(single);
			
			//将当期的第n个元素分别加入到result中第0-size的list中。如此迭代，最终求得所有的子集
			List<String> clone;
			for(int i=0;i<size;i++){
				clone=new ArrayList<String>();
				for(String rItem:result.get(i)){
					clone.add(rItem);
				}
				clone.add(source.get(source.size()-1));
				result.add(clone);
			}
		}
	}

	public static void main(String args[]){
		Apriori apriori =new Apriori();
		
		//获取频繁项集
		Map<String,Integer> frequentCollectionMap=apriori.getFC();
		
		//输出频繁项集
		System.out.println("-------------------频繁项集-------------------");
		Set<String> keySet=frequentCollectionMap.keySet();
		for(String key:keySet){
			System.out.println(key + " : " +frequentCollectionMap.get(key));
		}
		
		//获取关联规则
		Map<String,Double> relationRulesMap=apriori.getRelationRules(frequentCollectionMap);
		
		//输出强关联关系值对
		System.out.println("--------------------强关联---------------------");
		Set<String>mapSet =relationRulesMap.keySet();
		for(String item : mapSet){
			System.out.println(item+" : "+relationRulesMap.get(item));
		}
	}

}
