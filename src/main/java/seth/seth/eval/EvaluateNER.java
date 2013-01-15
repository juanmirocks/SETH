package seth.seth.eval;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: philippe
 * Date: 21.12.12
 * Time: 11:12
 * Evaluates prediction results of SETH on the corpus consisting of 210 articles from human mutation
 *
 */
public class EvaluateNER {

    public static void main(String[] args) throws IOException {

        String goldFolder = "/home/philippe/workspace/snp-normalizer/data/Results_Brat";
        String corpusResult=      "/home/philippe/workspace/thomas/seth/corpus/all.txt";

        Map<Integer, List<Entity>> goldstandardMap =  readGoldStandard(goldFolder);
        Map<Integer, List<Entity>> predictionMap =      readPredictions(corpusResult);

        System.out.println("Evaluating for " +goldstandardMap.size() +" abstracts");
        int tp =0; int fp=0; int fn=0;
        for(int pmid : goldstandardMap.keySet()){
            List<Entity> goldstandard = goldstandardMap.get(pmid);
            List<Entity> prediction =   predictionMap.get(pmid);
            if(prediction == null)
                prediction = new ArrayList<Entity>();

            for(Entity e : goldstandard){
                if(prediction.remove(e))
                    tp++;
                else{
                    fn++;
                    System.out.println(pmid +" " +e);
                }
            }
            fp+=prediction.size();

            //Inspect false positives
            //for(Entity p : prediction){
            //   System.out.println(pmid +" " +p);
            //}


        }
        DecimalFormat df = new DecimalFormat( "0.00" );
        System.err.println("TP " +tp);
        System.err.println("FP " +fp);
        System.err.println("FN " +fn);
        System.err.println("Recall " +df.format((double) tp/(tp+fn)));
        System.err.println("Precision " +df.format((double) tp/(tp+fp)));

    }

    /**
     * Loads SETH predictions
     * @return Predicted Entities
     * @throws IOException
     */
    private static Map<Integer, List<Entity>> readPredictions(String corpusResult) throws IOException {
        Map<Integer, List<Entity>> entityMap = new HashMap<Integer, List<Entity>>();

        BufferedReader br = new BufferedReader(new FileReader(new File(corpusResult)));
        while(br.ready()){
            String array[] = br.readLine().split("\t");
            int pmid = Integer.parseInt(array[0]);
            Entity entity = new Entity("", "SNP", Integer.parseInt(array[1]), Integer.parseInt(array[2]), array[3]);

            if(entityMap.containsKey(pmid))
                entityMap.get(pmid).add(entity);
            else{
                List<Entity> tmpList = new ArrayList<Entity>();
                tmpList.add(entity);
                entityMap.put(pmid, tmpList);
            }
        }

        return entityMap;
    }

    /**
     * Loads goldstandard annotations
     * @return  Goldstandard entities
     * @throws IOException
     */
    private static Map<Integer, List<Entity>> readGoldStandard(String goldFolder) throws IOException {

        File folder = new File(goldFolder);
        if(folder.isFile())
            throw new RuntimeException("Provide folder");

        Map<Integer, List<Entity>> entityMap = new HashMap<Integer, List<Entity>>();

        for(File file : folder.listFiles()){
            if(!file.getName().endsWith("ann"))
                continue;

            int pmid = Integer.parseInt(file.getName().substring(0,file.getName().indexOf(".")));

            BufferedReader br = new BufferedReader(new FileReader(file));
            while (br.ready()){
                String line = br.readLine();
                String array[] = line.split("\t");

                String annotation [] = array[1].split(" ");

                if(annotation[0].equals("SNP")){
                    Entity entity = new Entity(array[0], annotation[0], Integer.parseInt(annotation[1]), Integer.parseInt(annotation[2]), array[2]);

                    if(entityMap.containsKey(pmid))
                        entityMap.get(pmid).add(entity);

                    else{
                        List<Entity> tmpList = new ArrayList<Entity>();
                        tmpList.add(entity);
                        entityMap.put(pmid, tmpList);
                    }

                }
            }
            if(!entityMap.containsKey(pmid))
                entityMap.put(pmid, new ArrayList<Entity>());

        }
        return entityMap;
    }
}