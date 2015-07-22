# Add some used library:
library('XML')		# For XML files
library('tools')	# For the files' manipulation
library('raster')	# For the rasters' manipulation

#' Create a QGIS project (.qgs file) with all the .asc and all the .shp files found in the folder
#' @param folder string, folder to scan
#' @param file string, name of the QGIS project file
#' @param EPSG list, EPSG description of the reference systeme used as destination
#' @param Google boolean, true to add a Google Streets Layer at the QGIS Project
#' @return nothing
#' @author
#' Valentin SASYAN, v. 1.2.0, 06/22/2015
#' @examples
#' generate_qgs('data/generated/', 'qgis_project.qgs', EPSG, FALSE)
generate_qgs <- function(folder, file, EPSG, Google=TRUE) {

	asc = getASCinfo(folder)
	shp = getSHPinfo(folder)
	google = getGoogle(Google)

	extent <- asc[[1]]$extent
	for (i in shp) {extent <- union(extent, i$extent)}
	for (i in asc) {extent <- union(extent, i$extent)}

	top = newXMLNode('qgis', attrs=c(projectname="", version="2.8.2-Wien"))

		newXMLNode('title', '', parent=top)

		layer_tree_group = newXMLNode('layer-tree-group', attrs=c(expanded="1", checked="Qt::Checked", name=""), parent=top)
			newXMLNode('customproperties', parent=layer_tree_group)
			for (i in shp) {addChildren(layer_tree_group, getLayerTreeLayer(i$name, i$id))}
			for (i in asc) {addChildren(layer_tree_group, getLayerTreeLayer(i$name, i$id))}
			if (Google) {addChildren(layer_tree_group, getLayerTreeLayer(google$name, google$id))}

		newXMLNode('relation', parent=top)

		addChildren(top, getMapcanvas(xmin(extent), ymin(extent), xmax(extent), ymax(extent), EPSG))

		newXMLNode('visibility-presets', parent=top)

		layer_tree_canvas = newXMLNode('layer-tree-canvas', parent=top)
			custom_order = newXMLNode('custom-order', attrs=c(enabled="0"), parent=layer_tree_canvas)
				for (i in shp) {newXMLNode('item', i$id, parent=custom_order)}
				for (i in asc) {newXMLNode('item', i$id, parent=custom_order)}
				if (Google) {newXMLNode('item', google$id, parent=custom_order)}

		legend = newXMLNode('legend', attrs=c(updateDrawingOrder="true"), parent=top)
			for (i in shp) {addChildren(legend, getLegendlayer(i$name, i$id))}
			for (i in asc) {addChildren(legend, getLegendlayer(i$name, i$id))}
			if (Google) {addChildren(legend, getLegendlayer(google$name, google$id))}

		layercount <- length(asc) + length(shp)
		if (Google) {
			layercount <- layercount + 1
			ascOpacity <- 0.90
		} else {
			ascOpacity <- 1
		}
		projectlayers = newXMLNode('projectlayers', attrs=c(layercount=layercount), parent=top)
			for (i in shp) {
				addChildren(projectlayers, getMapLayerShp(
					i$id,
					i$filename,
					i$name,
					getSpatialrefsys(EPSG),
					c('x','y','z','id','type','svm','y_','z_','folder')
				))
			}
			for (i in asc) {
				addChildren(projectlayers, getMapLayerAsc(
					i$id,
					i$filename,
					i$name,
					getSpatialrefsys(EPSG),
					getNoData(1,3),
					getPipe(list(red=i$red,green=i$green,blue=i$blue), ascOpacity)
				))
			}
			if (Google) {
				addChildren(projectlayers, getMapLayerGoogle(
					google$id,
					google$name,
					getSpatialrefsys(google$EPSG)
				))
			}

		addChildren(top, getProperties(EPSG))

	saveXML(top, file=paste(folder,file,sep='/'), compression=0, indent=TRUE, prefix=NULL, doctype="<!DOCTYPE qgis PUBLIC 'http://mrcc.com/qgis.dtd' 'SYSTEM'>\n", encoding = getEncoding(top))
}

#' Create information for a Google Streets Layer
#' @param Google boolean, must create the information or not?
#' @return list, a list with all the information
#' @author
#' Valentin SASYAN, v. 1.0.0, 06/15/2015
#' @examples
#' google <- getGoogle(TRUE)
#' google <- getGoogle(FALSE)
getGoogle <- function(Google) {
	if (Google == TRUE) {
		google <- list(
			id = paste('OpenLayers_plugin_layer', gsub('[^0-9]','',Sys.time()), sep=''),
			name = 'Google Streets',
			EPSG = c(
				proj4 = '+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +wktext  +no_defs',
				srsid = '3857',
				srid = '3857',
				authid = 'EPSG:3857',
				description = 'WGS 84 / Pseudo Mercator',
				projectionacronym = 'merc',
				ellipsoidacronym = 'WGS84',
				geographicflag = 'false'
			)
		)
	} else {
		google <- list()
	}
	google
}

#' Get information for all the .asc files of the folder given for parameter
#' @param folder string, folder to scan
#' @return list, a list with all the information
#' @author
#' Valentin SASYAN, v. 1.0.1, 06/12/2015
#' @examples
#' getASCinfo('data/generated/')
getASCinfo <- function(folder) {
	liste = list.files(folder,'asc$',full.names=TRUE)
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

#' Get information for all the .shp files of the folder given for parameter
#' @param folder string, folder to scan
#' @return list, a list with all the information
#' @author
#' Valentin SASYAN, v. 1.0.1, 06/12/2015
#' @examples
#' getSHPinfo('data/generated/')
getSHPinfo <- function(folder) {
	liste = list.files(folder,'shp$',full.names=TRUE)
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
#' @return XMLNode, the XML element generated
#' @author
#' Valentin SASYAN, v. 1.0.0, 06/12/2015
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

#' Generate the 'maplayer' XML element of a Google Streets Layer
#' @param id string, id of the layer
#' @param layername string, layername of the layer
#' @param spatialrefsys XML element, specifications of the spatial reference systeme
#' @return XMLNode, the XML element generated
#' @author
#' Valentin SASYAN, v. 1.0.0, 06/15/2015
getMapLayerGoogle <- function(id, layername, spatialrefsys) {
	maplayer = newXMLNode('maplayer', attrs=c(minimumScale="0", maximumScale="1e+08", type="plugin", hasScaleBasedVisibilityFlag="0", name="openlayers"))
		newXMLNode('id', id, parent=maplayer)
		newXMLNode('datasource', '', parent=maplayer)

		newXMLNode('title', '', parent=maplayer)
		newXMLNode('abstract', '', parent=maplayer)

		keywordList = newXMLNode('keywordList', parent=maplayer)
			newXMLNode('value', '', parent=keywordList)

		newXMLNode('layername', layername, parent=maplayer)

		srs = newXMLNode('srs', parent=maplayer)
			addChildren(srs, spatialrefsys)

		customproperties = newXMLNode('customproperties', parent=maplayer)
			newXMLNode('property', attrs=c(key="ol_layer_type", value="Google Streets"), parent=customproperties)

	maplayer
}

#' Generate the 'maplayer' XML element of a .shp
#' @param id string, id of the layer
#' @param datasource string, datasource of the layer
#' @param layername string, layername of the layer
#' @param spatialrefsys XML element, specifications of the spatial reference systeme
#' @param editTypes list, list of the name of the file's band
#' @return XMLNode, the XML element generated
#' @author
#' Valentin SASYAN, v. 1.0.0, 06/12/2015
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
#' @return XMLNode, the XML element generated
#' @author
#' Valentin SASYAN, v. 1.0.0, 06/12/2015
getEditType <- function(editTypes) {
	et = newXMLNode('edittype')
		for (e in editTypes) {
			tps = newXMLNode('edittype', attrs=c(widgetv2type="TextEdit", name=e), parent=et)
				newXMLNode('widgetv2config', attrs=c(IsMultiline="0", fieldEditable="1", UseHtml="0", labelOnTop="0"), parent=tps)
		}
	et
}

#' Generate the 'renderer-v2' XML element of a .shp 'maplayer' XML element
#' @return XMLNode, the XML element generated
#' @author
#' Valentin SASYAN, v. 1.1.0, 06/22/2015
getRendererv2 <- function() {
	I <- length(symbolsList)
	renderer = newXMLNode('renderer-v2', attrs=c(attr="type", symbollevels="0", type="categorizedSymbol"))
		categoriesXML = newXMLNode('categories', parent=renderer)
			for (i in 1:I) {
				addChildren(categoriesXML, getCategory(i))
			}
		symbolsXML = newXMLNode('symbols', parent=renderer)
			for (i in 1:I) {
				addChildren(symbolsXML, getSymbol(i))
			}
	renderer
}

#' Generate the 'categroy' XML element of a .shp 'categories' XML element
#' @param i integer, indice of the symbol in the symbols list
#' @return XMLNode, the XML element generated
#' @author
#' Valentin SASYAN, v. 1.0.0, 06/22/2015
getCategory <- function(i) {
	newXMLNode('category', attrs=c(render="true", symbol=symbolsList[[i]][['symbol']], value=symbolsList[[i]][['value']], label=symbolsList[[i]][['label']]))
}

#' Generate the 'categroy' XML element of a .shp 'categories' XML element
#' @param i integer, indice of the symbol in the symbols list
#' @return XMLNode, the XML element generated
#' @author
#' Valentin SASYAN, v. 1.1.0, 06/23/2015
getSymbol <- function(i) {
	name <- 'circle'
	size <- '2'
	if (symbolsList[[i]][['symbol']] == 0) {
		name <- 'rectangle'
		size <- '2.5'
	}
	if (symbolsList[[i]][['symbol']] == 1) {
		name <- 'regular_star'
	}
	
	symbol = newXMLNode('symbol', attrs=c(alpha="1", type="marker", name=symbolsList[[i]][['symbol']]))
		layer = newXMLNode('layer', attrs=c(pass="0", class="SimpleMarker", locked="0"), parent=symbol)
			newXMLNode('prop', attrs=c(k="angle", v="0"), parent=layer)
			newXMLNode('prop', attrs=c(k="color", v=symbolsList[[i]][['color']]), parent=layer)
			newXMLNode('prop', attrs=c(k="horizontal_anchor_point", v="1"), parent=layer)
			newXMLNode('prop', attrs=c(k="name", v=name), parent=layer)
			newXMLNode('prop', attrs=c(k="offset", v="0,0"), parent=layer)
			newXMLNode('prop', attrs=c(k="offset_map_unit_scale", v="0,0"), parent=layer)
			newXMLNode('prop', attrs=c(k="offset_unit", v="MM"), parent=layer)
			newXMLNode('prop', attrs=c(k="outline_color", v="0,0,0,255"), parent=layer)
			newXMLNode('prop', attrs=c(k="outline_style", v="solid"), parent=layer)
			newXMLNode('prop', attrs=c(k="outline_width", v="0"), parent=layer)
			newXMLNode('prop', attrs=c(k="outline_width_map_unit_scale", v="0,0"), parent=layer)
			newXMLNode('prop', attrs=c(k="outline_width_unit", v="MM"), parent=layer)
			newXMLNode('prop', attrs=c(k="scale_method", v="area"), parent=layer)
			newXMLNode('prop', attrs=c(k="size", v=size), parent=layer)
			newXMLNode('prop', attrs=c(k="size_map_unit_scale", v="0,0"), parent=layer)
			newXMLNode('prop', attrs=c(k="size_unit", v="MM"), parent=layer)
			newXMLNode('prop', attrs=c(k="vertical_anchor_point", v="1"), parent=layer)
	symbol
}

#' Generate the 'spatialrefsys' XML element of a 'maplayer' XML element
#' @param EPSG list, EPSG description of the reference systeme used as destination
#' @return XMLNode, the XML element generated
#' @author
#' Valentin SASYAN, v. 1.1.0, 06/15/2015
getSpatialrefsys <- function(EPSG) {
	spatialrefsys = newXMLNode('spatialrefsys')
		newXMLNode('proj4', EPSG[['proj4']], parent=spatialrefsys)
		newXMLNode('srsid', EPSG[['srsid']], parent=spatialrefsys)
		newXMLNode('srid', EPSG[['srid']], parent=spatialrefsys)
		newXMLNode('authid', EPSG[['authid']], parent=spatialrefsys)
		newXMLNode('description', EPSG[['description']], parent=spatialrefsys)
		newXMLNode('projectionacronym', EPSG[['projectionacronym']], parent=spatialrefsys)
		newXMLNode('ellipsoidacronym', EPSG[['ellipsoidacronym']], parent=spatialrefsys)
		newXMLNode('geographicflag', EPSG[['geographicflag']], parent=spatialrefsys)
	spatialrefsys
}

#' Generate the 'pipe' XML element of a .asc 'maplayer' XML element
#' @param liste list, specification of the color used for each band
#' @param opacity double, opacity of the layer
#' @return XMLNode, the XML element generated
#' @author
#' Valentin SASYAN, v. 1.1.0, 06/15/2015
#' @examples
#' getPipe(list(red=c(min=-1,max=1),green=c(min=-0.84, max=0.42),blue=c(min=-0.42,max=0.84))
getPipe <- function(liste, opacity=1) {
	pipe = newXMLNode('pipe')
		rasterrenderer = newXMLNode('rasterrenderer', attrs=c(opacity=opacity, alphaBand="0", blueBand="3", greenBand="2", type="multibandcolor", redBand="-1"), parent=pipe)
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
#' @return XMLNode, the XML element generated
#' @author
#' Valentin SASYAN, v. 1.0.0, 06/12/2015
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
#' @return XMLNode, the XML element generated
#' @author
#' Valentin SASYAN, v. 1.0.0, 06/12/2015
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
#' @param EPSG list, EPSG description of the reference systeme used as destination
#' @return XMLNode, the XML element generated
#' @author
#' Valentin SASYAN, v. 1.1.0, 06/15/2015
getProperties <- function(EPSG) {
	properties = newXMLNode('properties')

		SpatialRefSys = newXMLNode('SpatialRefSys', parent=properties)
			newXMLNode('ProjectCRSProj4String', EPSG[['proj4']], attrs=c(type="QString"), parent=SpatialRefSys)
			newXMLNode('ProjectCrs', EPSG[['authid']], attrs=c(type="QString"), parent=SpatialRefSys)
			newXMLNode('ProjectCRSID', EPSG[['srid']], attrs=c(type="int"), parent=SpatialRefSys)

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
#' @return XMLNode, the XML element generated
#' @author
#' Valentin SASYAN, v. 1.0.0, 06/12/2015
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
#' @return XMLNode, the XML element generated
#' @author
#' Valentin SASYAN, v. 1.0.0, 06/12/2015
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
#' @param EPSG list, EPSG description of the reference systeme used as destination
#' @return XMLNode, the XML element generated
#' @author
#' Valentin SASYAN, v. 1.1.0, 06/15/2015
#' @examples
#' getMapcanvas(-42,-42,42,42)
getMapcanvas <- function(xmin, ymin, xmax, ymax, EPSG) {
	mapcanvas = newXMLNode('mapcanvas')
		newXMLNode('units', 'meters', parent=mapcanvas)
		extent = newXMLNode('extent', parent=mapcanvas)
			newXMLNode('xmin', xmin, parent=extent)
			newXMLNode('ymin', ymin, parent=extent)
			newXMLNode('xmax', xmax, parent=extent)
			newXMLNode('ymax', ymax, parent=extent)
		newXMLNode('rotation', '0', parent=mapcanvas)
		newXMLNode('projections', '1', parent=mapcanvas)
		destinationsrs = newXMLNode('destinationsrs', parent=mapcanvas)
			addChildren(destinationsrs, getSpatialrefsys(EPSG))
		newXMLNode('layer_coordinate_transform_info', parent=mapcanvas)
	mapcanvas
}


# Symboles for the classified shp
# @author
# Valentin SASYAN, v. 1.0.0, 06/22/2015
# NOT in the documentation
symbolsList <<- list(
	c(symbol="0", value="", label="", color="178,223,138,255"),
	c(symbol="1", value="1", label="W1", color="106,0,51,255"),
	c(symbol="2", value="2", label="W2", color="106,0,51,255"),
	c(symbol="3", value="11", label="PC1", color="255,0,0,255"),
	c(symbol="4", value="12", label="PC2H", color="97,215,223,255"),
	c(symbol="5", value="12", label="PC2L", color="97,215,223,255"),
	c(symbol="6", value="12", label="PC2M", color="97,215,223,255"),
	c(symbol="7", value="13", label="RM1L", color="97,215,223,255"),
	c(symbol="8", value="14", label="RM2H", color="97,215,223,255"),
	c(symbol="9", value="14", label="RM2L", color="97,215,223,255"),
	c(symbol="10", value="14", label="RM2M", color="0,0,255,255"),
	c(symbol="11", value="15", label="URML", color="255,0,0,255"),
	c(symbol="12", value="16", label="URMM", color="255,0,0,255"),
	c(symbol="13", value="3", label="S1M", color="124,124,124,255"),
	c(symbol="14", value="3", label="S1H", color="124,124,124,255"),
	c(symbol="15", value="3", label="S1L", color="124,124,124,255"),
	c(symbol="16", value="4", label="S2M", color="124,124,124,255"),
	c(symbol="17", value="4", label="S2L", color="124,124,124,255"),
	c(symbol="18", value="4", label="S2H", color="124,124,124,255"),
	c(symbol="19", value="5", label="S3", color="124,124,124,255"),
	c(symbol="20", value="6", label="S4H", color="124,124,124,255"),
	c(symbol="21", value="6", label="S4L", color="124,124,124,255"),
	c(symbol="22", value="6", label="S4M", color="124,124,124,255"),
	c(symbol="23", value="7", label="S5L", color="124,124,124,255"),
	c(symbol="24", value="7", label="S5M", color="124,124,124,255"),
	c(symbol="25", value="7", label="S5H", color="124,124,124,255"),
	c(symbol="26", value="8", label="C1H", color="97,215,223,255"),
	c(symbol="27", value="8", label="C1M", color="97,215,223,255"),
	c(symbol="28", value="8", label="C1L", color="97,215,223,255"),
	c(symbol="29", value="9", label="C2H", color="97,215,223,255"),
	c(symbol="30", value="9", label="C2L", color="97,215,223,255"),
	c(symbol="31", value="9", label="C2M", color="97,215,223,255"),
	c(symbol="32", value="10", label="C3H", color="97,215,223,255"),
	c(symbol="33", value="10", label="C3L", color="97,215,223,255"),
	c(symbol="34", value="10", label="C3M", color="97,215,223,255")
)