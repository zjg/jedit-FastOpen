package com.patelsoft.fastopen;

///////////////////////////////////////////////////////////////////////////////
//
// COPYRIGHT NOTICE:
//
//      Copyright (c) 2004 Zycus Infotech Pvt. Ltd.
//      All rights reserved.
//
//      This document and the information it contains is confidential and
//      proprietary to Zycus Infotech Pvt. Ltd.  Hence, it shall not be
//      used, copied, reproduced, transmitted, or stored in any form or by any
//      means, electronic, recording, photocopying, mechanical or otherwise,
//      without the prior written permission of Zycus Infotech Pvt.Ltd.
//
//////////////////////////////////////////////////////////////////////////////




/**
*	Copyright 2005, Zycus Inc. All rights Reserved.
*
*	@author jiger
*	@version 1.0
*	@created 19/Apr/2005
*/


public interface IndexListener
{
	public void indexingStarted(IndexManager manager);
	public void indexingCompleted(IndexManager manager);
}




