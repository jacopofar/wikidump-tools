POS tags from en.wiktionary
===========================

This is a set of small tools I created to extract part of speech tags from an XML dump of en.wiktionary and run simple NLP tasks on this data.

After downloading and expanding the XML dump of en.wiktionary, run generation.POSListGenerator on it (the command with no arguments will show an help).

It will produce a TSV file listing the terms and a list of possible part of speech separated by commas.

You can see a list generated from the dump of march 2013.


Using tagging.POSTagProcessedWikiText the POS dictionary can be used to tag a text file. It will be split in terms usign spaces and punctuation and each term will be tagged (when many tags are available, they will be listed separated by comma, when the term is unknown will be marked as UNKNOWN).

The tagger *recognize terms with multiple words*, like "buona sera", when present in the dictionary (I counted 10254 of them in the provided one), trying to match the longest sequence posible.

These tools are made for *Italian language*, but should be easy to adapt to other European Languages (more difficult for Chinese or Japanese due to lack of spaces between words, would need a tokener).

To obtain a text representation from a Wikipedia dump (which uses wiki markup), suitable for the tagger, the repository contains a Python3 script, WikiExtractor.py (usage `python3 Wikiextractor.py wiki-articles-dump.xml output-file.txt`), originally developed by Pisa University ([here](http://medialab.di.unipi.it/wiki/Wikipedia_Extractor)).
