# social_health
This is the project that was originally designed for tracing the development of the subject of social health over time. To go about this, we collected literature data from Scopus with the query "social health."

After that, we extracted the reference list from each publication and collected titles and abstracts. In addition, we collected titles and abstracts of the publications that cited the publications that include 
"social health" in their titles. In this way, we preserved the chain of influences among publications.

In this project, we included data collection module, LDA module based on Mallet, and analysis module with Stanford CoreNLP. Since the size of corenlp-model exceeds the limit of size, for those who want to use this codebase, please download stanford-corenlp-3.6.0-models.jar from Stanford CoreNLP website.
