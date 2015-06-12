# Add some used library:
library('XML')		# For XML files
library('tools')	# For the files' manipulation
library('raster')	# For the rasters' manipulation

#' Create a QGIS project (.qgs file) with all the .asc and all the .shp files found in the folder
#' @param folder string, folder to scan
#' @param file string, name of the QGIS project file
#' @return nothing
#' @author Valentin SASYAN
#' @version 1.0.0
#' @date  06/12/2015
#' @examples
#' generate_qgs('data/generated/','qgis_project.qgs')
generate_qgs <- function(folder, file) {

	asc = getASCinfo(folder)
	shp = getSHPinfo(folder)

	extent <- asc[[1]]$extent
	for (i in shp) {extent <- union(extent, i$extent)}
	for (i in asc) {extent <- union(extent, i$extent)}

	top = newXMLNode('qgis', attrs=c(projectname="", version="2.8.2-Wien"))

		newXMLNode('title', '', parent=top)

		layer_tree_group = newXMLNode('layer-tree-group', attrs=c(expanded="1", checked="Qt::Checked", name=""), parent=top)
			newXMLNode('customproperties', parent=layer_tree_group)
			for (i in shp) {addChildren(layer_tree_group, getLayerTreeLayer(i$name, i$id))}
			for (i in asc) {addChildren(layer_tree_group, getLayerTreeLayer(i$name, i$id))}

		newXMLNode('relation', parent=top)

		addChildren(top, getMapcanvas(xmin(extent), ymin(extent), xmax(extent), ymax(extent)))

		newXMLNode('visibility-presets', parent=top)

		layer_tree_canvas = newXMLNode('layer-tree-canvas', parent=top)
			custom_order = newXMLNode('custom-order', attrs=c(enabled="0"), parent=layer_tree_canvas)
				for (i in shp) {newXMLNode('item', i$id, parent=custom_order)}
				for (i in asc) {newXMLNode('item', i$id, parent=custom_order)}

		legend = newXMLNode('legend', attrs=c(updateDrawingOrder="true"), parent=top)
			for (i in shp) {addChildren(legend, getLegendlayer(i$name, i$id))}
			for (i in asc) {addChildren(legend, getLegendlayer(i$name, i$id))}

		projectlayers = newXMLNode('projectlayers', attrs=c(layercount='3'), parent=top)
			for (i in shp) {
				addChildren(projectlayers, getMapLayerShp(
					i$id,
					i$filename,
					i$name,
					getSpatialrefsys(),
					c('x','y','z')
				))
			}
			for (i in asc) {
				addChildren(projectlayers, getMapLayerAsc(
					i$id,
					i$filename,
					i$name,
					getSpatialrefsys(),
					getNoData(1,3),
					getPipe(list(red=i$red,green=i$green,blue=i$blue))
				))
			}

		addChildren(top, getProperties())

	saveXML(top, file=paste(folder,file,sep='/'), compression=0, indent=TRUE, prefix=NULL, doctype="<!DOCTYPE qgis PUBLIC 'http://mrcc.com/qgis.dtd' 'SYSTEM'>\n", encoding = getEncoding(top))
}

#' Get information for all the .asc files of the folder passed in parameter
#' @param folder string, folder to scan
#' @return a list with all the informations
#' @author Valentin SASYAN
#' @version 1.0.0
#' @date  06/12/2015
#' @examples
#' getASCinfo('data/generated/')
getASCinfo <- function(folder) {
	liste = list.files('data//generated','asc$',full.names=TRUE)
	rListe = c(lapply(liste, function(file) {
		raster <- raster(file)
		extent <- extent(raster)

		raster <- raster(file, band=1)
		raster <- setMinMax(raster)
		minR <- minValue(raster)
		maxR <- maxValue(raster)

		raster <- raster(file, band=2)
		raster <- setMinMax(raster)
		minG <- minValue(raster)
		maxG <- maxValue(raster)

		raster <- raster(file, band=3)
		raster <- setMinMax(raster)
		minB <- minValue(raster)
		maxB <- maxValue(raster)
		
		list(
			extent=extent,
			red=newXMLNode('rasterTransparency'),#c(min=minR,max=maxR),
			green=c(min=minG,max=maxG),
			blue=c(min=minB,max=maxB),
			filename=paste('.',basename(file),sep='/'),
			name=basename(file_path_sans_ext(file)),
			id=paste(basename(file_path_sans_ext(file)),gsub('[^0-9]','',file.mtime(file)),sep='')

		)
	}))
}

#' Get information for all the .shp files of the folder passed in parameter
#' @param folder string, folder to scan
#' @return a list with all the informations
#' @author Valentin SASYAN
#' @version 1.0.0
#' @date  06/12/2015
#' @examples
#' getSHPinfo('data/generated/')
getSHPinfo <- function(folder) {
	liste = list.files('data//generated','shp$',full.names=TRUE)
	rListe = c(lapply(liste, function(file) {
		filename <- paste('/',basename(file),sep='')
		name <- basename(file_path_sans_ext(file))
		data <- readOGR(gsub(filename, '', file),layer=name)
		list(
			extent=extent(data),
			filename=paste('.',filename,sep='/'),
			name=name,
			id=paste(name,gsub('[^0-9]','',file.mtime(file)),sep='')
		)
	}))
}

#' Generate the 'maplayer' XML element of a .asc
#' @param id string, id of the layer
#' @param datasource string, datasource of the layer
#' @param layername string, layername of the layer
#' @param spatialrefsys XML element, specifications of the spatial reference systeme
#' @param noData XML element, number of the data
#' @param pipe XML element, specifications of the colorisation of the layer
#' @return a XML element
#' @author Valentin SASYAN
#' @version 1.0.0
#' @date  06/12/2015
getMapLayerAsc <- function(id, datasource, layername, spatialrefsys, noData, pipe) {
	maplayer = newXMLNode('maplayer', attrs=c(minimumScale="0", maximumScale="1e+08", type="raster", hasScaleBasedVisibilityFlag="0"))
		newXMLNode('id', id, parent=maplayer)
		newXMLNode('datasource', datasource, parent=maplayer)

		newXMLNode('title', '', parent=maplayer)
		newXMLNode('abstract', '', parent=maplayer)

		keywordList = newXMLNode('keywordList', parent=maplayer)
			newXMLNode('value', '', parent=keywordList)

		newXMLNode('layername', layername, parent=maplayer)

		srs = newXMLNode('srs', parent=maplayer)
			addChildren(srs, spatialrefsys)

		customproperties = newXMLNode('customproperties', parent=maplayer)
			newXMLNode('value', attrs=c(key="identify/format", value="Value"), parent=customproperties)

		newXMLNode('provider', 'gdal', parent=maplayer)

		addChildren(maplayer, noData)

		mapLayerStyleManager = newXMLNode('map-layer-style-manager', attrs=c(current=""), parent=maplayer)
			newXMLNode('map-layer-style', attrs=c(name=""), parent=mapLayerStyleManager)
		
		addChildren(maplayer, pipe)

		newXMLNode('blendMode', '0', parent=maplayer)

	maplayer
}

#' Generate the 'maplayer' XML element of a .shp
#' @param id string, id of the layer
#' @param datasource string, datasource of the layer
#' @param layername string, layername of the layer
#' @param spatialrefsys XML element, specifications of the spatial reference systeme
#' @param editTypes list, list of the name of the file's band
#' @return a XML element
#' @author Valentin SASYAN
#' @version 1.0.0
#' @date  06/12/2015
getMapLayerShp <- function(id, datasource, layername, spatialrefsys, editTypes) {
	maplayer = newXMLNode('maplayer', attrs=c(minimumScale="0", maximumScale="1e+08", simplifyDrawingHints="0", minLabelScale="0", maxLabelScale="1e+08", simplifyDrawingTol="1", geometry="Point", simplifyMaxScale="1", type="vector", hasScaleBasedVisibilityFlag="0", simplifyLocal="1", scaleBasedLabelVisibilityFlag="0"))
		newXMLNode('id', id, parent=maplayer)
		newXMLNode('datasource', datasource, parent=maplayer)

		newXMLNode('title', '', parent=maplayer)
		newXMLNode('abstract', '', parent=maplayer)

		keywordList = newXMLNode('keywordList', parent=maplayer)
			newXMLNode('value', '', parent=keywordList)

		newXMLNode('layername', layername, parent=maplayer)

		srs = newXMLNode('srs', parent=maplayer)
			addChildren(srs, spatialrefsys)

		newXMLNode('provider', 'ogr', attrs=c(encoding="system"), parent=maplayer)

		newXMLNode('previewExpression', '', parent=maplayer)

		newXMLNode('vectorjoins', parent=maplayer)

		newXMLNode('expressionfields', parent=maplayer)

		mapLayerStyleManager = newXMLNode('map-layer-style-manager', attrs=c(current=""), parent=maplayer)
			newXMLNode('map-layer-style', attrs=c(name=""), parent=mapLayerStyleManager)
		
		addChildren(maplayer, getEditType(editTypes))
		
		addChildren(maplayer, getRendererv2())

	maplayer
}

#' Generate the 'edittype' XML element of a .shp 'maplayer' XML element
#' @param editTypes list, list of the name of the file's band
#' @return a XML element
#' @author Valentin SASYAN
#' @version 1.0.0
#' @date  06/12/2015
getEditType <- function(editTypes) {
	et = newXMLNode('edittype')
		for (e in editTypes) {
			tps = newXMLNode('edittype', attrs=c(widgetv2type="TextEdit", name=e), parent=et)
				newXMLNode('widgetv2config', attrs=c(IsMultiline="0", fieldEditable="1", UseHtml="0", labelOnTop="0"), parent=tps)
		}
	et
}

#' Generate the 'renderer-v2' XML element of a .shp 'maplayer' XML element
#' @return a XML element
#' @author Valentin SASYAN
#' @version 1.0.0
#' @date  06/12/2015
getRendererv2 <- function() {
	renderer = newXMLNode('renderer-v2', attrs=c(symbollevels="0", type="singleSymbol"))
		symbols = newXMLNode('symbols', parent=renderer)
			symbol = newXMLNode('symbol', attrs=c(alpha="1", type="marker", name="0"), parent=symbols)
				layer = newXMLNode('layer', attrs=c(pass="0", class="SimpleMarker", locked="0"), parent=symbol)
					newXMLNode('prop', attrs=c(k="angle", v="0"), parent=layer)
					newXMLNode('prop', attrs=c(k="color", v="227,26,28,255"), parent=layer)
					newXMLNode('prop', attrs=c(k="horizontal_anchor_point", v="1"), parent=layer)
					newXMLNode('prop', attrs=c(k="name", v="circle"), parent=layer)
					newXMLNode('prop', attrs=c(k="offset", v="0,0"), parent=layer)
					newXMLNode('prop', attrs=c(k="offset_map_unit_scale", v="0,0"), parent=layer)
					newXMLNode('prop', attrs=c(k="offset_unit", v="MM"), parent=layer)
					newXMLNode('prop', attrs=c(k="outline_color", v="0,0,0,255"), parent=layer)
					newXMLNode('prop', attrs=c(k="outline_style", v="solid"), parent=layer)
					newXMLNode('prop', attrs=c(k="outline_width", v="0"), parent=layer)
					newXMLNode('prop', attrs=c(k="outline_width_map_unit_scale", v="0,0"), parent=layer)
					newXMLNode('prop', attrs=c(k="outline_width_unit", v="MM"), parent=layer)
					newXMLNode('prop', attrs=c(k="scale_method", v="area"), parent=layer)
					newXMLNode('prop', attrs=c(k="size", v="0.6"), parent=layer)
					newXMLNode('prop', attrs=c(k="size_map_unit_scale", v="0,0"), parent=layer)
					newXMLNode('prop', attrs=c(k="size_unit", v="MM"), parent=layer)
					newXMLNode('prop', attrs=c(k="vertical_anchor_point", v="1"), parent=layer)
	renderer
}

#' Generate the 'spatialrefsys' XML element of a 'maplayer' XML element
#' @return a XML element
#' @author Valentin SASYAN
#' @version 1.0.0
#' @date  06/12/2015
getSpatialrefsys <- function() {
	spatialrefsys = newXMLNode('spatialrefsys')
		newXMLNode('proj4', '+proj=longlat +datum=WGS84 +no_defs', parent=spatialrefsys)
		newXMLNode('srsid', '3452', parent=spatialrefsys)
		newXMLNode('srid', '4326', parent=spatialrefsys)
		newXMLNode('authid', 'EPSG:4326', parent=spatialrefsys)
		newXMLNode('description', 'WGS 84', parent=spatialrefsys)
		newXMLNode('projectionacronym', 'longlat', parent=spatialrefsys)
		newXMLNode('ellipsoidacronym', 'WGS84', parent=spatialrefsys)
		newXMLNode('geographicflag', 'true', parent=spatialrefsys)
	spatialrefsys
}

#' Generate the 'pipe' XML element of a .asc 'maplayer' XML element
#' @param liste list, specification of the color used for each band
#' @return a XML element
#' @author Valentin SASYAN
#' @version 1.0.0
#' @date  06/12/2015
#' @examples
#' getPipe(list(red=c(min=-1,max=1),green=c(min=-0.84, max=0.42),blue=c(min=-0.42,max=0.84))
getPipe <- function(liste) {
	pipe = newXMLNode('pipe')
		rasterrenderer = newXMLNode('rasterrenderer', attrs=c(opacity="1", alphaBand="0", blueBand="3", greenBand="2", type="multibandcolor", redBand="-1"), parent=pipe)
			if (is.vector(liste[['red']])) {
				addChildren(rasterrenderer, getContrastEnhancement('red', liste[['red']]))
			} else {
				newXMLNode('rasterTransparency', parent=rasterrenderer)
			}
			if (is.vector(liste[['green']])) {
				addChildren(rasterrenderer, getContrastEnhancement('green', liste[['green']]))
			} else {
				newXMLNode('rasterTransparency', parent=rasterrenderer)
			}
			if (is.vector(liste[['blue']])) {
				addChildren(rasterrenderer, getContrastEnhancement('blue', liste[['blue']]))
			} else {
				newXMLNode('rasterTransparency', parent=rasterrenderer)
			}
		newXMLNode('brightnesscontrast', attrs=c(brightness="0", contrast="0"), parent=pipe)
		newXMLNode('huesaturation', attrs=c(colorizeGreen="128", colorizeOn="0", colorizeRed="255", colorizeBlue="128", grayscaleMode="0", saturation="0", colorizeStrength="100"), parent=pipe)
		newXMLNode('rasterresampler', attrs=c(maxOversampling="2"), parent=pipe)
	pipe
}

#' Generate the 'ContrastEnhancement' XML element of a pipe XML element
#' @param color string, name of the color
#' @param liste list, min and max for this color
#' @return a XML element
#' @author Valentin SASYAN
#' @version 1.0.0
#' @date  06/12/2015
#' @examples
#' getContrastEnhancement('green', c(min=-0.84, max=0.42))
getContrastEnhancement <- function(color, liste) {	
	contrastEnhancement = newXMLNode(paste(color, 'ContrastEnhancement', sep=''))
		newXMLNode('minValue', paste(liste[['min']], sep=''), parent=contrastEnhancement)
		newXMLNode('maxValue', paste(liste[['max']], sep=''), parent=contrastEnhancement)
		newXMLNode('algorithm', 'StretchToMinimumMaximum', parent=contrastEnhancement)
	contrastEnhancement
}

#' Generate the 'noData' XML element of a .asc 'maplayer' XML element
#' @param min int, first number of the data
#' @param max int, last number of the data
#' @return a XML element
#' @author Valentin SASYAN
#' @version 1.0.0
#' @date  06/12/2015
#' @examples
#' getNoData(1,3)
getNoData<- function(min, max) {
	noData = newXMLNode('noData')
	for (i in min:max) {
		newXMLNode('noDataList', attrs=c(bandNo=i, useSrcNoData="0"), parent=noData)
	}
	noData
}

#' Generate the 'properties' XML element of a .asc 'maplayer' XML element
#' @return a XML element
#' @author Valentin SASYAN
#' @version 1.0.0
#' @date  06/12/2015
getProperties <- function() {
	properties = newXMLNode('properties')

		SpatialRefSys = newXMLNode('SpatialRefSys', parent=properties)
			newXMLNode('ProjectCRSProj4String', '+proj=longlat +datum=WGS84 +no_defs', attrs=c(type="QString"), parent=SpatialRefSys)
			newXMLNode('ProjectCrs', 'EPSG:4326', attrs=c(type="QString"), parent=SpatialRefSys)
			newXMLNode('ProjectCRSID', '3452', attrs=c(type="int"), parent=SpatialRefSys)

		Paths = newXMLNode('Paths', parent=properties)
			newXMLNode('Absolute', 'false', attrs=c(type="bool"), parent=Paths)

		Gui = newXMLNode('Gui', parent=properties)
			newXMLNode('SelectionColorBluePart', '0', attrs=c(type="int"), parent=Gui)
			newXMLNode('CanvasColorGreenPart', '255', attrs=c(type="int"), parent=Gui)
			newXMLNode('CanvasColorRedPart', '255', attrs=c(type="int"), parent=Gui)
			newXMLNode('SelectionColorRedPart', '255', attrs=c(type="int"), parent=Gui)
			newXMLNode('SelectionColorAlphaPart', '255', attrs=c(type="int"), parent=Gui)
			newXMLNode('SelectionColorGreenPart', '255', attrs=c(type="int"), parent=Gui)
			newXMLNode('CanvasColorBluePart', '255', attrs=c(type="int"), parent=Gui)

		Digitizing = newXMLNode('Digitizing', parent=properties)
			newXMLNode('DefaultSnapToleranceUnit', '2', attrs=c(type="int"), parent=Digitizing)
			newXMLNode('LayerSnappingList', attrs=c(type="QStringList"), parent=Digitizing)
			newXMLNode('LayerSnappingEnabledList', attrs=c(type="QStringList"), parent=Digitizing)
			newXMLNode('SnappingMode', 'current_layer', attrs=c(type="QString"), parent=Digitizing)
			newXMLNode('AvoidIntersectionsList', attrs=c(type="QStringList"), parent=Digitizing)
			tps = newXMLNode('LayerSnappingToleranceUnitList', attrs=c(type="QStringList"), parent=Digitizing)
				newXMLNode('value', '2', parent=tps)
			tps = newXMLNode('LayerSnapToList', attrs=c(type="QStringList"), parent=Digitizing)
				newXMLNode('value', 'to_vertex', parent=tps)
			newXMLNode('DefaultSnapType', 'off', attrs=c(type="QString"), parent=Digitizing)
			newXMLNode('DefaultSnapTolerance', '0', attrs=c(type="double"), parent=Digitizing)
			tps = newXMLNode('LayerSnappingToleranceList', attrs=c(type="QStringList"), parent=Digitizing)
				newXMLNode('value', '0.000000', parent=tps)

		PositionPrecision = newXMLNode('PositionPrecision', parent=properties)
			newXMLNode('DecimalPlaces', '2', attrs=c(type="int"), parent=PositionPrecision)
			newXMLNode('Automatic', 'true', attrs=c(type="bool"), parent=PositionPrecision)

		Legend = newXMLNode('Legend', parent=properties)
			newXMLNode('filterByMap', 'false', attrs=c(type="bool"), parent=Legend)

	properties
}

#' Generate the 'noData' XML element of a 'legend' XML element
#' @param Name string, name of the layer
#' @param ID string, id of the layer
#' @return a XML element
#' @author Valentin SASYAN
#' @version 1.0.0
#' @date  06/12/2015
#' @examples
#' getLegendlayer('exportedData','exportedData20150612104242')
getLegendlayer <- function(Name, ID) {
	legendlayer = newXMLNode('legendlayer', attrs=c(drawingOrder="-1", open="true", checked="Qt::Checked", name=Name, showFeatureCount="0"))
		filegroup = newXMLNode('filegroup', attrs=c(open="true", hidden="false"), parent=legendlayer)
			newXMLNode('legendlayerfile', attrs=c(isInOverview="0", layerid=ID, visible="1"), parent=filegroup)
	legendlayer
}

#' Generate the 'layer-tree-layer' XML element of a 'layer-tree-group' XML element
#' @param Name string, name of the layer
#' @param ID string, id of the layer
#' @return a XML element
#' @author Valentin SASYAN
#' @version 1.0.0
#' @date  06/12/2015
#' @examples
#' getLayerTreeLayer('exportedData','exportedData20150612104242')
getLayerTreeLayer <- function(Name, ID) {
	LayerTreeLayer = newXMLNode('layer-tree-layer', attrs=c(expanded="1", checked="Qt::Checked", id=ID, name=Name))
		newXMLNode('customproperties', parent=LayerTreeLayer)
	LayerTreeLayer
}

#' Generate the 'mapcanvas' XML element of a 'qgis' XML element
#' @param xmin double, xmin limit of the map canevas
#' @param ymin double, ymin limit of the map canevas
#' @param xmax double, xmax limit of the map canevas
#' @param ymax double, ymax limit of the map canevas
#' @return a XML element
#' @author Valentin SASYAN
#' @version 1.0.0
#' @date  06/12/2015
#' @examples
#' getMapcanvas(-42,-42,42,42)
getMapcanvas <- function(xmin, ymin, xmax, ymax) {
	mapcanvas = newXMLNode('mapcanvas')
		newXMLNode('units', 'degrees', parent=mapcanvas)
		extent = newXMLNode('extent', parent=mapcanvas)
			newXMLNode('xmin', xmin, parent=extent)
			newXMLNode('ymin', ymin, parent=extent)
			newXMLNode('xmax', xmax, parent=extent)
			newXMLNode('ymax', ymax, parent=extent)
		newXMLNode('rotation', '0', parent=mapcanvas)
		newXMLNode('projections', '0', parent=mapcanvas)
		destinationsrs = newXMLNode('destinationsrs', parent=mapcanvas)
			addChildren(destinationsrs, getSpatialrefsys())
		newXMLNode('layer_coordinate_transform_info', parent=mapcanvas)
	mapcanvas
}