#' Qualify the given classification
#' @param proj.classif SpatialPointsDataFrame, the points classification
#' @return nothing
#' @author
#' Valentin SASYAN, v. 1.0.1, 07/22/2015
qualifyClassif <- function(proj.classif) {

	# Separate 1) the data used as model and 2) the data to process:
	classif <- as.data.frame(subset(proj.classif, svm == 'classif'))
	listId <- unique(classif$id)
	cat('\nClassification information:')

	for (Id in listId) {
		classifTemp <- subset(classif, id == Id)
		factor <- factor(classifTemp$type)
		s <- sort(summary(factor), decreasing=T)
		cat(paste('\nBuilding #', Id, ': ', listTypeBuilding[[as.integer(labels(s)[[1]])]], sep=''))
		cat(paste('\nSummary for Building #', Id, ':\n', sep=''))
		print(s)
	}
	cat('\n\n')
}