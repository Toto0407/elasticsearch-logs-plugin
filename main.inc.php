<?php
/* Copyright (C) 2000-2007 Rodolphe Quiedeville <rodolphe@quiedeville.org>
 * Copyright (C) 2003      Jean-Louis Bergamo   <jlb@j1b.org>
 * Copyright (C) 2004-2011 Laurent Destailleur  <eldy@users.sourceforge.net>
 * Copyright (C) 2004      Sebastien Di Cintio  <sdicintio@ressource-toi.org>
 * Copyright (C) 2004      Benoit Mortier       <benoit.mortier@opensides.be>
 * Copyright (C) 2004      Christophe Combelles <ccomb@free.fr>
 * Copyright (C) 2005-2010 Regis Houssin        <regis@dolibarr.fr>
 * Copyright (C) 2008      Raphael Bertrand (Resultic)       <raphael.bertrand@resultic.fr>
 * Copyright (C) 2010      Juanjo Menent        <jmenent@2byte.es>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * or see http://www.gnu.org/
 */

/**
 *	\file			htdocs/lib/functions.lib.php
 *	\brief			A set of functions for Dolibarr
 *					This file contains all frequently used functions.
 *	\version		$Id: functions.lib.php,v 1.552 2011/08/04 22:01:23 eldy Exp $
 */

// For compatibility during upgrade
if (! defined('DOL_DOCUMENT_ROOT'))	    define('DOL_DOCUMENT_ROOT', '..');
if (! defined('ADODB_DATE_VERSION'))    include_once(DOL_DOCUMENT_ROOT."/includes/adodbtime/adodb-time.inc.php");



/**
 *  Return value of a param into GET or POST supervariable
 *  @param          paramname   Name of parameter to found
 *  @param			check		Type of check (''=no check,  'int'=check it's numeric, 'alpha'=check it's alpha only)
 *  @param			method		Type of method (0 = get then post, 1 = only get, 2 = only post, 3 = post then get)
 *  @return         string      Value found or '' if check fails
 */
function GETPOST($paramname,$check='',$method=0)
{
    if (empty($method)) $out = isset($_GET[$paramname])?$_GET[$paramname]:(isset($_POST[$paramname])?$_POST[$paramname]:'');
    elseif ($method==1) $out = isset($_GET[$paramname])?$_GET[$paramname]:'';
    elseif ($method==2) $out = isset($_POST[$paramname])?$_POST[$paramname]:'';
    elseif ($method==3) $out = isset($_POST[$paramname])?$_POST[$paramname]:(isset($_GET[$paramname])?$_GET[$paramname]:'');

    if (!empty($check))
    {
        // Check if numeric
        if ($check == 'int' && ! preg_match('/^[-\.,0-9]+$/i',trim($out))) $out='';
        // Check if alpha
        //if ($check == 'alpha' && ! preg_match('/^[ =:@#\/\\\(\)\-\._a-z0-9]+$/i',trim($out))) $out='';
        if ($check == 'alpha' && preg_match('/"/',trim($out))) $out='';    // Only " is dangerous because param in url can close the href= or src= and add javascript functions
    }

    return $out;
}

?>