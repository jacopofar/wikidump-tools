__ARCHIVED:__ this project should still works but it's much easier to use [Wiktextract](https://github.com/tatuylonen/wiktextract), it works across multiple languages and it provides up-to-date extracts

POS tags from en.wiktionary
===========================

This is a set of small tools to extract part of speech tags from an XML dump of en.wiktionary and run simple NLP tasks on this data.

The supported operations are three:

* Converting an XML dump of Wikipedia (using wiki markup) in plain text
* Extracting a POS dictionary for Italian from an en.wiktionary dump
* Tagging a plain text file using the POS dictionary (no Machine Learning, just pure string matching)

After downloading compressed the XML dump of en.wiktionary, run `generation.POSListGenerator` on it (the command with no arguments will show an help, it works both on the compressed file and the XML).

It will produce a TSV file listing the terms and a list of possible part of speech separated by commas.

A list generated from the dump of may 2014 is directly available in this repository.

Using `tagging.POSTagProcessedWikiText` the POS dictionary can be used to tag a text file. It will be split in terms usign spaces and punctuation and each term will be tagged (when many tags are available, they will be listed separated by comma, when the term is unknown will be marked as UNKNOWN).

The tagger recognizes terms with multiple words, like "buona sera", when present in the dictionary (there were more than 10000 of them in 2013), in order to match the longest sequence available.

These tools are made for *Italian language*, but should be easy to adapt to other European Languages (more difficult for Chinese or Japanese due to lack of spaces between words, would need a tokener which is not included).

To obtain a text representation from a Wikipedia dump (which uses wiki markup), suitable for the tagger, the repository contains a Python3 script, WikiExtractor.py (usage `python3 Wikiextractor.py wiki-articles-dump.xml.bz2 output-file.txt`, you can also pass the uncompressed XML file), originally developed by Pisa University ([here](http://medialab.di.unipi.it/wiki/Wikipedia_Extractor)).
