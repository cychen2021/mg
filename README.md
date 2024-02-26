# MG+

## Project Structure

- The constraints used in the comparative study are available in `data/comparative_study/constraints.xml`.
- Example constraints and contexts for the case study are in `data/case_study/constraints.xml`, `data/case_study/context_defs.xml`, and `data/case_study/context_data/`.

Note that we cannot release the full data we used in the case study due to privacy issues. The example data in `data/case_study` will give a illustration of how to use the tool.

## Running the Tool

Please notify that we removed the code that counts the ULRs and the numbers of evaluated truth values because they severely slow down the checking process and  lead to imprecision in the measured checking time. We separately measured the three metrics in our evaluation, uncommenting the specific code for one metric each time. We only includes the code that measures the checking time in the released code. It is easy to manually add the code for the other two metrics if needed.

Use the following commands to run the tool on the example data:

```bash
./gradlew app:run --args="-m N -c ../data/case_study/config -s 10 -e 10"
```

The checking results and statistics will be in the `output` directory. The checking results will be recorded `<MethodName>.txt` and the statistics will be recorded `<MethodName>.csv`, where `<MethodName>` can be:

- `ECC-CG`: ECC x CG
- `ECC-OG`: ECC x OG
- `ECC-MG`: ECC x MG
- `ECC-MG+`: ECC x MG+
- `PCC-CG`: PCC x CG
- `PCC-OG`: PCC x OG
- `PCC-MG-1`: PCC x MG
- `PCC-MG+-4`: PCC x MG+
- `ConC-CG`: Con-C x CG
- `ConC-OG`: Con-C x OG
- `ConC-MG`: Con-C x MG
- `ConC-MG+`: Con-C x MG+
