#' Qualify the given classification
#' @param proj.classif SpatialPointsDataFrame, the points classification
#' @return nothing
#' @author Valentin SASYAN
#' @version 1.0.0
#' @date  06/25/2015
qualifyClassif <- function(proj.classif, debug=FALSE) {

	# Separate 1) the data used as model and 2) the data to process:
	classif <- as.data.frame(subset(proj.classif, svm == 'classif'))
	listId <- unique(classif$id)
	print('\nClassification information:')

	for (Id in listId) {
		classifTemp <- subset(classif, id == Id)
		factor <- factor(classifTemp$type)
		s <- sort(summary(factor), decreasing=T)
		print(paste('\nBuilding #', Id, ': ', listTypeBuilding[[as.integer(labels(s)[[1]])]], sep=''))
		print(paste('Sunnary for Building #', Id, ':', sep=''))
		print(s)
	}
	print('\nEND\n')
}