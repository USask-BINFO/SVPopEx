# SVPopEx

## Overview
SVPopEx is a Java-based application for the visualization and exploration of structural variants (SVs) at the population-level. SVPopEx visualizes insertions, deletions, inversions, duplications, and translocation calls from a multi-sample VCF file alongside gene annotations from a GFF3 file. 

## Dependencies 
- SVPopEx requires Java version 21 or higher to be installed on your computer.

## Installation
1. Go to the latest release: https://github.com/USask-BINFO/SVPopEx/releases/latest.
2. Click 'Assets'.
3. Click on the .jar file to download it.
4. Double-click on the .jar file in your file explorer to launch SVPopEx.



## Usage
### VCF Specifications: 
- Formatting must conform to the following specifications:
  - Both single sample and multi-sample VCF files are supported. All samples must be included in the header and genotype information for each SV. 
  -  The INFO field must contain SVTYPE with a value of: INS, DEL, INV, DUP, TRA, or BND.
  -  The INFO field must contain SVLEN and a corresponding value. 
  -  The meta-information lines must contain the ##contig lines and the chromosomes will be shown in that order.
  -  Sample names must not contain periods. 
  -  The ID for each SV must be unique.
- VCFs generated from cuteSV and SVIM are compatible with SVPopEx.

### GFF3 Specifications:
- Formatting must conform to the following specifications:
  - The attributes field must contain the ID tag and a corresponding value.
  - Currently only features of type 'gene' are supported.
 
### Uploading a File
Get started by uploading a VCF file. Then, gene annotations in a GFF3 file can be uploaded. 
<div align="center">
  <img width="650" height="364" alt="upload" src="https://github.com/user-attachments/assets/d3638ef7-9f0b-4d2f-b0d7-e56249ce0135">
</div>

## License
This project is licensed under the [GNU General Public License v3.0](LICENSE).

## Citation
```bibtex
@article{baker2026svpopex,
  author    = {Baker, Mari and Bett, Kirstin and Vargas, Ana and Jin, Lingling},
  title     = {SVPopEx: Population-Wide Visualization and Exploration of Structural Variants},
  year      = {2026},
  publisher = {Cold Spring Harbor Laboratory},
  journal = {bioRxiv},
  doi = {https://doi.org/10.64898/2026.08.08.743609}
}
```

## Contact
For any questions or inquires, feel free to open an [Issue](https://github.com/USask-BINFO/SVPopEx/issues) or contact [mcb508@mail.usask.ca](mailto:mcb508@mail.usask.ca).
